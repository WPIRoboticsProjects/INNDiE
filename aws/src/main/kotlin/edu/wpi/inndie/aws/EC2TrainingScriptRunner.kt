package edu.wpi.inndie.aws

import edu.wpi.inndie.db.data.TrainingScriptProgress
import edu.wpi.inndie.tfdata.Dataset
import edu.wpi.inndie.util.FilePath
import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.koin.core.KoinComponent
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
    private val progressReporter =
        EC2TrainingScriptProgressReporter(
            ec2Manager,
            s3Manager
        )
    private val canceller =
        EC2TrainingScriptCanceller(ec2Manager)
    private val resultSupplier =
        EC2TrainingResultSupplier(s3Manager)

    override fun startScript(
        config: RunTrainingScriptConfiguration
    ) {
        require(config.oldModelName is FilePath.S3) {
            "Must start from a model in S3. Got: ${config.oldModelName}"
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

        s3Manager.uploadTrainingScript(scriptFileName, config.scriptContents)

        // Reset the training progress so the script doesn't start in the completed state
        s3Manager.setTrainingProgress(config.id, "not started")

        // Remove the heartbeat so we know if the script set it
        s3Manager.removeHeartbeat(config.id)

        // Clear the log file so we know what the instance wrote
        s3Manager.clearTrainingLogFile(config.id)

        // We need to download custom datasets from S3. Example datasets will be downloaded
        // by the script using Keras.
        val downloadDatasetString = when (config.dataset) {
            is Dataset.ExampleDataset -> ""
            is Dataset.Custom ->
                """inndie download-dataset "${config.dataset.path.path}""""
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
            |pip3 install https://github.com/wpilibsuite/INNDiE-cli/releases/download/v0.1.18/inndie-0.1.18-py2.py3-none-any.whl
            |inndie create-heartbeat ${config.id}
            |inndie update-training-progress ${config.id} "initializing"
            |inndie download-model "${config.oldModelName.path}"
            |$downloadDatasetString
            |inndie download-training-script "$scriptFileName"
            |docker run -v ${'$'}(eval "pwd"):/home wpilib/inndie-ci:latest "/usr/bin/python3.6" "/home/$scriptFileName"
            |inndie upload-training-results ${config.id} "${config.workingDir}"
            |inndie update-training-progress ${config.id} "completed"
            |inndie remove-heartbeat ${config.id}
            |inndie set-training-log-file ${config.id} "/var/log/syslog"
            |shutdown -h now
            """.trimMargin()

        LOGGER.debug {
            """
            |Sending script to EC2:
            |$scriptForEC2
            |
            """.trimMargin()
        }

        val instanceId = ec2Manager.startTrainingInstance(scriptForEC2, instanceType)
        instanceIds[config.id] = instanceId
        scriptDataMap[config.id] = config
        progressReporter.addJob(config.id, instanceId, config.epochs)
        canceller.addJob(config.id, instanceId)
    }

    override fun listResults(id: Int) = resultSupplier.listResults(id)

    override fun getResult(id: Int, filename: String) = resultSupplier.getResult(id, filename)

    fun getInstanceId(jobId: Int): String {
        requireJobIsInMaps(jobId)
        return instanceIds[jobId]!!
    }

    override fun getTrainingProgress(jobId: Int) = progressReporter.getTrainingProgress(jobId)

    override fun overrideTrainingProgress(jobId: Int, progress: TrainingScriptProgress) =
        progressReporter.overrideTrainingProgress(jobId, progress)

    override fun cancelScript(jobId: Int) = canceller.cancelScript(jobId)

    private fun requireJobIsInMaps(jobId: Int) {
        require(jobId in instanceIds.keys)
        require(jobId in scriptDataMap.keys)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
