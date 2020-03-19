package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.util.getLatestEpochFromProgressCsv
import java.lang.NumberFormatException
import mu.KotlinLogging
import software.amazon.awssdk.services.ec2.model.InstanceStateName

/**
 * A [TrainingScriptProgressReporter] that is designed for an [EC2TrainingScriptRunner].
 *
 * @param ec2Manager Used to interface with EC2.
 * @param s3Manager Used to interface with S3.
 */
class EC2TrainingScriptProgressReporter(
    private val ec2Manager: EC2Manager,
    private val s3Manager: S3Manager
) : TrainingScriptProgressReporter {

    private val instanceIds = mutableMapOf<Int, String>()
    private val overriddenProgressMap = mutableMapOf<Int, TrainingScriptProgress>()
    private val epochsMap = mutableMapOf<Int, Int>()

    /**
     * Adds a Job.
     *
     * @param jobId The ID of the Job.
     * @param instanceId The EC2 instance ID for the instance that was started to run the training
     * script.
     * @param epochs The number of epochs the Job will train for.
     */
    fun addJob(jobId: Int, instanceId: String, epochs: Int) {
        instanceIds[jobId] = instanceId
        epochsMap[jobId] = epochs
    }

    @UseExperimental(ExperimentalStdlibApi::class)
    override fun getTrainingProgress(jobId: Int): TrainingScriptProgress {
        requireJobIsInMaps(jobId)

        // If the progress is being overridden, return that progress data early
        overriddenProgressMap[jobId]?.let { return it }

        val instanceId = instanceIds[jobId]!!
        return computeTrainingScriptProgress(
            heartbeat = s3Manager.getHeartbeat(jobId),
            progress = s3Manager.getTrainingProgress(jobId),
            status = ec2Manager.getInstanceState(instanceId),
            ec2ConsoleOutput = s3Manager.getTrainingLogFile(jobId),
            epochs = epochsMap[jobId]!!
        )
    }

    override fun overrideTrainingProgress(jobId: Int, progress: TrainingScriptProgress) {
        overriddenProgressMap[jobId] = progress
    }

    private fun requireJobIsInMaps(jobId: Int) {
        require(jobId in instanceIds.keys)
        require(jobId in epochsMap.keys)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }

        internal fun computeTrainingScriptProgress(
            heartbeat: String,
            progress: String,
            status: InstanceStateName?,
            ec2ConsoleOutput: String,
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
                return TrainingScriptProgress.Error(
                    "The EC2 instance stopped without exiting cleanly.\n$ec2ConsoleOutput"
                )
            }

            return when (heartbeat) {
                "0" -> when (progress) {
                    "not started", "completed" -> progressAssumingEverythingIsFine
                    else -> TrainingScriptProgress.Error(
                        "Unknown progress text: $progress\n$ec2ConsoleOutput"
                    )
                }

                "1" -> when (progress) {
                    "not started" -> when (status) {
                        InstanceStateName.RUNNING -> progressAssumingEverythingIsFine
                        else -> TrainingScriptProgress.Error(
                            "Unknown progress text: $progress\n$ec2ConsoleOutput"
                        )
                    }

                    "initializing" -> progressAssumingEverythingIsFine

                    else -> when (status) {
                        InstanceStateName.SHUTTING_DOWN, InstanceStateName.TERMINATED,
                        InstanceStateName.STOPPING -> TrainingScriptProgress.Error(
                            "The EC2 instance stopped without exiting cleanly.\n$ec2ConsoleOutput"
                        )

                        else -> progressAssumingEverythingIsFine
                    }
                }

                else -> TrainingScriptProgress.Error(
                    "Unknown heartbeat text: $heartbeat.\n$ec2ConsoleOutput"
                )
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
                    TrainingScriptProgress.InProgress(
                        getLatestEpochFromProgressCsv(progress) / epochs.toDouble(),
                        progress
                    )
                } catch (ex: NumberFormatException) {
                    TrainingScriptProgress.Error("Invalid progress text: $progress")
                } catch (ex: IllegalStateException) {
                    TrainingScriptProgress.Error("Invalid progress text: $progress")
                }
            } else {
                LOGGER.warn {
                    "Training progress error. heartbeat=$heartbeat, progress=$progress"
                }
                TrainingScriptProgress.Error("Invalid progress text: $progress")
            }
        }
    }
}
