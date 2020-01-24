package edu.wpi.axon.aws

import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import java.util.Base64
import java.util.concurrent.atomic.AtomicLong
import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.koin.core.KoinComponent
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.ec2.model.InstanceType
import software.amazon.awssdk.services.ec2.model.ShutdownBehavior

/**
 * A [TrainingScriptRunner] that runs the training script on EC2 and hosts datasets and models on
 * S3. This implementation requires that the script does not try to manage models with S3 itself:
 * this class will handle all of that. The script should just load and save the model from/to its
 * current directory.
 *
 * @param instanceType The type of the EC2 instance to run the training script on.
 */
class EC2TrainingScriptRunner(
    bucketName: String,
    private val instanceType: InstanceType // TODO: Move this to [startScript]
) : TrainingScriptRunner, KoinComponent {

    private val ec2 by lazy { Ec2Client.builder().build() }
    private val s3Manager = S3Manager(bucketName)

    private val nextScriptId = AtomicLong()
    private val instanceIds = mutableMapOf<Long, String>()
    private val scriptDataMap = mutableMapOf<Long, RunTrainingScriptConfiguration>()
    // Tracks whether the script entered the InProgress state at least once
    private val scriptStarted = mutableMapOf<Long, Boolean>()

    override fun startScript(
        runTrainingScriptConfiguration: RunTrainingScriptConfiguration
    ): Long {
        // Check for if the script uses the CLI to manage the model in S3. This class is supposed to
        // own working with S3.
        require(
            !runTrainingScriptConfiguration.scriptContents.contains("download_model") &&
                !runTrainingScriptConfiguration.scriptContents.contains("upload_model")
        ) {
            """
            |Cannot start the script because it interfaces with AWS:
            |${runTrainingScriptConfiguration.scriptContents}
            |
            """.trimMargin()
        }

        // The file name for the generated script
        val scriptFileName = "${RandomStringUtils.randomAlphanumeric(20)}.py"

        val newModelName = runTrainingScriptConfiguration.newModelName
        val datasetName = runTrainingScriptConfiguration.dataset.nameForS3ProgressReporting

        s3Manager.uploadTrainingScript(
            scriptFileName,
            runTrainingScriptConfiguration.scriptContents
        )

        // Reset the training progress so the script doesn't start in the completed state
        s3Manager.setTrainingProgress(newModelName, datasetName, "not started")

        // Remove the heartbeat so we know if the script set it
        s3Manager.removeHeartbeat(newModelName, datasetName)

        // We need to download custom datasets from S3. Example datasets will be downloaded
        // by the script using Keras.
        val downloadDatasetString = when (runTrainingScriptConfiguration.dataset) {
            is Dataset.ExampleDataset -> ""
            is Dataset.Custom ->
                """axon download-dataset "${runTrainingScriptConfiguration.dataset.pathInS3}""""
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
            |axon download-untrained-model "${runTrainingScriptConfiguration.oldModelName}"
            |$downloadDatasetString
            |axon download-training-script "$scriptFileName"
            |docker run -v ${'$'}(eval "pwd"):/home wpilib/axon-ci:latest "/usr/bin/python3.6 /home/$scriptFileName"
            |axon upload-trained-model "$newModelName"
            |axon update-training-progress "$newModelName" "$datasetName" "completed"
            |axon remove-heartbeat "$newModelName" "$datasetName"
            |shutdown -h now
            """.trimMargin()

        LOGGER.info {
            """
            |Sending script to EC2:
            |$scriptForEC2
            |
            """.trimMargin()
        }

        return ec2.runInstances {
            it.imageId("ami-04b9e92b5572fa0d1")
                .instanceType(instanceType)
                .maxCount(1)
                .minCount(1)
                .userData(scriptForEC2.toBase64())
                .securityGroups("axon-autogenerated-ec2-sg")
                .instanceInitiatedShutdownBehavior(ShutdownBehavior.TERMINATE)
                .iamInstanceProfile { it.name("axon-autogenerated-ec2-instance-profile") }
        }.let {
            val scriptId = nextScriptId.getAndIncrement()
            instanceIds[scriptId] = it.instances().first().instanceId()
            scriptDataMap[scriptId] = runTrainingScriptConfiguration
            scriptStarted[scriptId] = false
            scriptId
        }
    }

    @UseExperimental(ExperimentalStdlibApi::class)
    override fun getTrainingProgress(scriptId: Long): TrainingScriptProgress {
        require(scriptId in instanceIds.keys)

        val runTrainingScriptConfiguration = scriptDataMap[scriptId]
            ?: error("BUG: scriptId missing from scriptDataMap")

        val newModelName = runTrainingScriptConfiguration.newModelName
        val datasetName = runTrainingScriptConfiguration.dataset.nameForS3ProgressReporting

        val heartbeat = s3Manager.getHeartbeat(newModelName, datasetName)
        return when (val progress = s3Manager.getTrainingProgress(newModelName, datasetName)) {
            "not started" -> TrainingScriptProgress.NotStarted
            "initializing" -> TrainingScriptProgress.Initializing
            "completed" -> TrainingScriptProgress.Completed
            else -> if (heartbeat == "1") {
                TrainingScriptProgress.InProgress(
                    progress.toDouble() / runTrainingScriptConfiguration.epochs
                )
            } else {
                LOGGER.warn {
                    "Training progress error. heartbeat=$heartbeat, progress=$progress"
                }
                TrainingScriptProgress.Error
            }
        }
    }

    private fun String.toBase64() =
        Base64.getEncoder().encodeToString(byteInputStream().readAllBytes())

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
