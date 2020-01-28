package edu.wpi.axon.aws

import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.util.FilePath
import java.lang.NumberFormatException
import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.koin.core.KoinComponent
import software.amazon.awssdk.services.ec2.model.InstanceStateName
import software.amazon.awssdk.services.ec2.model.InstanceType

/**
 * A [TrainingScriptRunner] that runs the training script on EC2 and hosts datasets and models on
 * S3. This implementation requires that the script does not try to manage models with S3 itself:
 * this class will handle all of that. The script should just load and save the model from/to its
 * current directory.
 *
 * @param instanceType The type of the EC2 instance to run the training script on.
 * @param ec2Manager Used to interface with EC2.
 * @param s3Manager Used to interface with S3.
 */
class EC2TrainingScriptRunner(
    private val instanceType: InstanceType,
    private val ec2Manager: EC2Manager,
    private val s3Manager: S3Manager
) : TrainingScriptRunner, KoinComponent {

    private val instanceIds = mutableMapOf<Int, String>()
    private val scriptDataMap = mutableMapOf<Int, RunTrainingScriptConfiguration>()

    override fun startScript(
        config: RunTrainingScriptConfiguration
    ) {
        require(config.oldModelName is FilePath.S3) {
            "Must start from a model in S3. Got: ${config.oldModelName}"
        }
        require(config.newModelName is FilePath.S3) {
            "Must export to a model in S3. Got: ${config.newModelName}"
        }
        require(config.epochs > 0) {
            "Must train for at least one epoch. Got ${config.epochs} epochs."
        }
        when (config.dataset) {
            is Dataset.Custom -> require(config.dataset.path is FilePath.S3) {
                "Custom datasets must be in S3. Got non-local dataset: ${config.dataset}"
            }
        }

        // The file name for the generated script
        @Suppress("MagicNumber")
        val scriptFileName = "${RandomStringUtils.randomAlphanumeric(20)}.py"

        val newModelName = config.newModelName.filename
        val datasetName = config.dataset.progressReportingName

        s3Manager.uploadTrainingScript(scriptFileName, config.scriptContents)

        // Reset the training progress so the script doesn't start in the completed state
        s3Manager.setTrainingProgress(config.id, "not started")

        // Remove the heartbeat so we know if the script set it
        s3Manager.removeHeartbeat(config.id)

        // We need to download custom datasets from S3. Example datasets will be downloaded
        // by the script using Keras.
        val downloadDatasetString = when (config.dataset) {
            is Dataset.ExampleDataset -> ""
            is Dataset.Custom ->
                """axon download-dataset "${config.dataset.path.path}""""
        }

        val scriptForEC2 = """
            |#!/bin/bash
            |exec 1> >(logger -s -t ${'$'}(basename ${'$'}0)) 2>&1
            |apt update
            |apt install -y build-essential curl libcurl4-openssl-dev libssl-dev wget \
            |   python3.6 python3-pip python3-dev apt-transport-https ca-certificates \
            |   software-properties-common
            |curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
            |add-apt-repository -y "deb [arch=amd64] https://download.docker.com/linux/ubuntu bionic stable"
            |apt update
            |apt-cache policy docker-ce
            |apt install -y docker-ce
            |systemctl status docker
            |pip3 install https://github.com/wpilibsuite/axon-cli/releases/download/v0.1.11/axon-0.1.11-py2.py3-none-any.whl
            |axon create-heartbeat "$newModelName" "$datasetName"
            |axon update-training-progress "$newModelName" "$datasetName" "initializing"
            |axon download-untrained-model "${config.oldModelName.path}"
            |$downloadDatasetString
            |axon download-training-script "$scriptFileName"
            |docker run -v ${'$'}(eval "pwd"):/home wpilib/axon-ci:latest "/usr/bin/python3.6 /home/$scriptFileName"
            |axon upload-trained-model "$newModelName"
            |axon update-training-progress "$newModelName" "$datasetName" "completed"
            |axon remove-heartbeat "$newModelName" "$datasetName"
            |shutdown -h now
            """.trimMargin()

        LOGGER.debug {
            """
            |Sending script to EC2:
            |$scriptForEC2
            |
            """.trimMargin()
        }

        instanceIds[config.id] = ec2Manager.startTrainingInstance(scriptForEC2, instanceType)
        scriptDataMap[config.id] = config
    }

    @UseExperimental(ExperimentalStdlibApi::class)
    override fun getTrainingProgress(jobId: Int): TrainingScriptProgress {
        requireJobIsInMaps(jobId)
        return computeTrainingScriptProgress(
            heartbeat = s3Manager.getHeartbeat(jobId),
            progress = s3Manager.getTrainingProgress(jobId),
            status = ec2Manager.getInstanceState(instanceIds[jobId]!!),
            epochs = scriptDataMap[jobId]!!.epochs
        )
    }

    override fun cancelScript(jobId: Int) {
        requireJobIsInMaps(jobId)
        ec2Manager.terminateInstance(instanceIds[jobId]!!)
    }

    private fun requireJobIsInMaps(jobId: Int) {
        require(jobId in instanceIds.keys)
        require(jobId in scriptDataMap.keys)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }

        internal fun computeTrainingScriptProgress(
            heartbeat: String,
            progress: String,
            status: InstanceStateName?,
            epochs: Int
        ): TrainingScriptProgress {
            LOGGER.debug {
                """
                |Heartbeat: $heartbeat
                |Progress: $progress
                |Instance status: $status
                """.trimMargin()
            }

            val progressAssumingEverythingIsFine = computeProgressAssumingEverythingIsFine(
                heartbeat,
                progress,
                status,
                epochs
            )

            if ((status == InstanceStateName.SHUTTING_DOWN ||
                    status == InstanceStateName.TERMINATED ||
                    status == InstanceStateName.STOPPING) &&
                (heartbeat != "0" || progress != "completed")
            ) {
                return TrainingScriptProgress.Error
            }

            return when (heartbeat) {
                "0" -> when (progress) {
                    "not started", "completed" -> progressAssumingEverythingIsFine
                    else -> TrainingScriptProgress.Error
                }

                "1" -> when (progress) {
                    "initializing" -> progressAssumingEverythingIsFine
                    "not started" -> TrainingScriptProgress.Error

                    else -> when (status) {
                        InstanceStateName.SHUTTING_DOWN, InstanceStateName.TERMINATED,
                        InstanceStateName.STOPPING -> TrainingScriptProgress.Error

                        else -> progressAssumingEverythingIsFine
                    }
                }

                else -> TrainingScriptProgress.Error
            }
        }

        private fun computeProgressAssumingEverythingIsFine(
            heartbeat: String,
            progress: String,
            status: InstanceStateName?,
            epochs: Int
        ) = when (progress) {
            "not started" -> when (status) {
                InstanceStateName.PENDING -> TrainingScriptProgress.Creating
                InstanceStateName.RUNNING -> TrainingScriptProgress.Initializing
                else -> TrainingScriptProgress.Creating
            }

            "initializing" -> TrainingScriptProgress.Initializing
            "completed" -> TrainingScriptProgress.Completed

            else -> if (heartbeat == "1") {
                try {
                    TrainingScriptProgress.InProgress(progress.toDouble() / epochs)
                } catch (ex: NumberFormatException) {
                    TrainingScriptProgress.Error
                }
            } else {
                LOGGER.warn {
                    "Training progress error. heartbeat=$heartbeat, progress=$progress"
                }
                TrainingScriptProgress.Error
            }
        }
    }
}
