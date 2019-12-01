package edu.wpi.axon.aws

import arrow.fx.IO
import arrow.fx.extensions.fx
import edu.wpi.axon.dbdata.TrainingScriptProgress
import java.util.Base64
import java.util.concurrent.atomic.AtomicLong
import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.koin.core.KoinComponent
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.ec2.model.InstanceStateName
import software.amazon.awssdk.services.ec2.model.InstanceType
import software.amazon.awssdk.services.ec2.model.ShutdownBehavior
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

/**
 * A [TrainingScriptRunner] that runs the training script on EC2 and hosts datasets and models on
 * S3. This implementation requires that the script does not try to manage models with S3 itself:
 * this class will handle all of that. The script should just load and save the model from/to its
 * current directory.
 *
 * @param bucketName The S3 bucket name to use for dataset and models.
 * @param instanceType The type of the EC2 instance to run the training script on.
 * @param region The region to connect to, or `null` to autodetect the region.
 */
class EC2TrainingScriptRunner(
    private val bucketName: String,
    private val instanceType: InstanceType,
    private val region: Region?
) : TrainingScriptRunner, KoinComponent {

    private val s3 by lazy { S3Client.builder().apply { region?.let { region(it) } }.build() }
    private val ec2 by lazy { Ec2Client.builder().apply { region?.let { region(it) } }.build() }

    private val nextScriptId = AtomicLong()
    private val instanceIds = mutableMapOf<Long, String>()
    private val scriptDataMap = mutableMapOf<Long, ScriptDataForEC2>()

    override fun startScript(scriptDataForEC2: ScriptDataForEC2): IO<Long> {
        // Check for if the script uses the CLI to manage the model in S3. This class is supposed to
        // own working with S3.
        if (scriptDataForEC2.scriptContents.contains("download_model_file") ||
            scriptDataForEC2.scriptContents.contains("upload_model_file")
        ) {
            return IO.raiseError(
                IllegalArgumentException(
                    """
                    |Cannot start the script because it interfaces with AWS:
                    |${scriptDataForEC2.scriptContents}
                    |
                    """.trimMargin()
                )
            )
        }

        // The file name for the generated script
        val scriptFileName = "${RandomStringUtils.randomAlphanumeric(20)}.py"

        return IO.fx {
            IO {
                s3.putObject(
                    PutObjectRequest.builder().bucket(bucketName)
                        .key("axon-uploaded-training-scripts/$scriptFileName").build(),
                    RequestBody.fromString(scriptDataForEC2.scriptContents)
                )
            }.bind()

            val downloadDatasetString = scriptDataForEC2.datasetPathInS3.fold(
                { "" },
                {
                    """axon download-dataset "$it" "$bucketName""""
                }
            )

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
                |pip3 install https://github.com/wpilibsuite/axon-cli/releases/download/v0.1.4/axon-0.1.4-py2.py3-none-any.whl
                |axon download-model-file "${scriptDataForEC2.oldModelName}" "$bucketName"
                |$downloadDatasetString
                |axon download-training-script "$scriptFileName" "$bucketName"
                |docker run -v ${'$'}(eval "pwd"):/home wpilib/axon-ci:latest "/usr/bin/python3.6 /home/$scriptFileName"
                |axon upload-model-file "${scriptDataForEC2.newModelName}" "$bucketName"
                |shutdown -h now
                """.trimMargin()

            LOGGER.info {
                """
                |Sending script to EC2:
                |$scriptForEC2
                |
                """.trimMargin()
            }

            IO {
                ec2.runInstances {
                    it.imageId("ami-04b9e92b5572fa0d1")
                        .instanceType(instanceType)
                        .maxCount(1)
                        .minCount(1)
                        .userData(scriptForEC2.toBase64())
                        .securityGroups("axon-ec2-autogenerated")
                        .instanceInitiatedShutdownBehavior(ShutdownBehavior.TERMINATE)
                        .iamInstanceProfile { it.name("axon-ec2-role-manual") }
                }.let {
                    val scriptId = nextScriptId.getAndIncrement()
                    instanceIds[scriptId] = it.instances().first().instanceId()
                    scriptId
                }
            }.bind()
        }
    }

    @UseExperimental(ExperimentalStdlibApi::class)
    override fun getTrainingProgress(scriptId: Long): IO<TrainingScriptProgress> =
        if (scriptId in instanceIds.keys) {
            IO {
                val status = ec2.describeInstanceStatus {
                    it.instanceIds(instanceIds[scriptId]!!)
                }.instanceStatuses().first()

                when (status.instanceState().name()) {
                    // TODO: Make a REST API call here to get the percent complete. The API might not be up yet
                    InstanceStateName.RUNNING -> TrainingScriptProgress.InProgress(0.0)

                    InstanceStateName.SHUTTING_DOWN, InstanceStateName.TERMINATED,
                    InstanceStateName.STOPPING, InstanceStateName.STOPPED ->
                        TrainingScriptProgress.Completed

                    else -> TrainingScriptProgress.NotStarted
                }
            }
        } else {
            IO.raiseError(UnsupportedOperationException("Script id $scriptId not found."))
        }

    private fun String.toBase64() =
        Base64.getEncoder().encodeToString(byteInputStream().readAllBytes())

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
