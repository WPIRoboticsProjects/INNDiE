package edu.wpi.axon.aws

import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.typeclasses.ExitCase
import mu.KotlinLogging
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.ec2.model.InstanceType
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.ssm.SsmClient

class TestAWS {

    fun uploadAndStartScript(script: String): IO<Unit> {
        val s3 = S3Client.builder()
            .region(Region.US_EAST_1)
            .build()

        // The bucket to store things in
        val bucketName = "axon-salmon-testbucket1"

        // The file name for the generated script
        val scriptFileName = "testobject1"

        return IO.fx {
            IO {
                s3.putObject(
                    // TODO: This will replace the content if the file already exists
                    PutObjectRequest.builder().bucket(bucketName)
                        .key(scriptFileName).build(),
                    RequestBody.fromString(script)
                )
            }.bind()

            val ec2 = Ec2Client.builder()
                .region(Region.US_EAST_1)
                .build()

            IO {
                ec2.runInstances {
                    it.imageId("ami-0b69ea66ff7391e80")
                        .instanceType(InstanceType.T2_MICRO)
                        .maxCount(1)
                        .minCount(1)
                }
            }.bracketCase(
                { startInstanceResponse, exitCase ->
                    when (exitCase) {
                        is ExitCase.Completed, ExitCase.Canceled -> IO {
                            ec2.stopInstances {
                                it.instanceIds(startInstanceResponse.instances().map { it.instanceId() })
                            }

                            Unit
                        }

                        is ExitCase.Error -> IO {
                            ec2.stopInstances {
                                it.instanceIds(startInstanceResponse.instances().map { it.instanceId() })
                            }

                            logger.catching(exitCase.e)
                        }
                    }
                },
                {
                    IO {
                        val instance = it.instances().first()
                        val ssm = SsmClient.builder()
                            .region(Region.US_EAST_1)
                            .build()

                        ssm.sendCommand {
                            it.instanceIds(instance.instanceId())
                                .documentName("AWS-RunShellScript")
                                .parameters(emptyMap())
                                .outputS3BucketName(bucketName)
                        }
                    }
                }
            ).bind()

            Unit
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
