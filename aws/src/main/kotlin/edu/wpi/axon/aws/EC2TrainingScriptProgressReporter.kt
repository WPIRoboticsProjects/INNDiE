package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress
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
    private val scriptDataMap = mutableMapOf<Int, RunTrainingScriptConfiguration>()
    private val overriddenProgressMap = mutableMapOf<Int, TrainingScriptProgress>()

    /**
     * Adds a Job.
     *
     * @param config The config the Job was started with.
     * @param instanceId The EC2 instance ID for the instance that was started to run the training
     * script.
     */
    fun addJob(config: RunTrainingScriptConfiguration, instanceId: String) {
        instanceIds[config.id] = instanceId
        scriptDataMap[config.id] = config
    }

    @UseExperimental(ExperimentalStdlibApi::class)
    override fun getTrainingProgress(jobId: Int): TrainingScriptProgress {
        requireJobIsInMaps(jobId)

        // If the progress is being overridden, return that progress data early
        overriddenProgressMap[jobId]?.let { return it }

        return computeTrainingScriptProgress(
            heartbeat = s3Manager.getHeartbeat(jobId),
            progress = s3Manager.getTrainingProgress(jobId),
            status = ec2Manager.getInstanceState(instanceIds[jobId]!!),
            epochs = scriptDataMap[jobId]!!.epochs
        )
    }

    override fun overrideTrainingProgress(jobId: Int, progress: TrainingScriptProgress) {
        overriddenProgressMap[jobId] = progress
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
