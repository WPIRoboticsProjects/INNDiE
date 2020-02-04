package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.util.createLocalProgressFilepath
import java.io.File
import java.lang.NumberFormatException
import mu.KotlinLogging

/**
 * A [TrainingScriptProgressReporter] that is designed for a [LocalTrainingScriptRunner].
 *
 * @param progressReportingDirPrefix The prefix for the local progress reporting directory.
 */
class LocalTrainingScriptProgressReporter(
    private val progressReportingDirPrefix: String = "/tmp/progress_reporting"
) : TrainingScriptProgressReporter {

    private val scriptDataMap = mutableMapOf<Int, RunTrainingScriptConfiguration>()
    // TODO: Use a callback to get the most recent progress instead of this
    private var scriptProgressMapMap = mutableMapOf<Int, Map<Int, TrainingScriptProgress>?>()
    private val scriptThreadMap = mutableMapOf<Int, Thread?>()
    private val overriddenProgressMap = mutableMapOf<Int, TrainingScriptProgress>()

    /**
     * Adds a Job that was just created.
     *
     * @param config The config the Job was started with.
     * @param progressMap The progress reporting map the training thread will write updates to.
     * @param thread The training thread.
     */
    fun addJob(
        config: RunTrainingScriptConfiguration,
        progressMap: Map<Int, TrainingScriptProgress>,
        thread: Thread
    ) {
        scriptDataMap[config.id] = config
        scriptProgressMapMap[config.id] = progressMap
        scriptThreadMap[config.id] = thread
    }

    /**
     * Adds a Job that was pulled from the DB after Axon was restarted (so there is no progressMap
     * or thread data).
     *
     * @param config The config the Job was started with.
     */
    fun addJobAfterRestart(
        config: RunTrainingScriptConfiguration
    ) {
        scriptDataMap[config.id] = config
        scriptProgressMapMap[config.id] = null
        scriptThreadMap[config.id] = null
    }

    @Suppress("MapGetWithNotNullAssertionOperator")
    override fun getTrainingProgress(jobId: Int): TrainingScriptProgress {
        requireJobIsInMaps(jobId)

        // If the progress is being overridden, return that progress data early
        overriddenProgressMap[jobId]?.let { return it }

        val progressMap = scriptProgressMapMap[jobId]
        val epochs = scriptDataMap[jobId]!!.epochs

        // Create the progress file up here to share code but don't read from it unless we have to,
        // to avoid reading from it if it's not there but didn't have to be
        val progressFile = File(createLocalProgressFilepath(progressReportingDirPrefix, jobId))

        return if (progressMap == null) {
            // We are resuming from a restart because there was no initial progress data. Remove
            // whitespace from the progress text because `toInt` is picky.
            val progressText = progressFile.readText().replace(Regex("\\s+"), "")
            try {
                LOGGER.debug {
                    """
                    progressText=$progressText
                    epochs=$epochs
                    """.trimIndent()
                }
                val progressDouble = progressText.toInt()
                if (progressDouble == epochs) {
                    // We need to make sure the progress gets to Completed because the thread is no
                    // longer alive to do it for us.
                    TrainingScriptProgress.Completed
                } else {
                    TrainingScriptProgress.InProgress(progressDouble / epochs.toDouble())
                }
            } catch (ex: NumberFormatException) {
                TrainingScriptProgress.Error("Invalid progress text: $progressText")
            }
        } else {
            // If progressMap wasn't null, then the thread should've been set as well
            require(scriptThreadMap[jobId] != null)

            val lastProgressData = progressMap[jobId]!!
            if (scriptThreadMap[jobId]!!.isAlive) {
                // Training thread is still running. Try to read the progress file. Remove
                // whitespace because `toInt` is picky.
                val progressText = progressFile.readText().replace(Regex("\\s+"), "")

                when {
                    lastProgressData == TrainingScriptProgress.Creating &&
                        progressText == "initializing" -> {
                        // It's initializing if the training thread wrote Creating and the progress file
                        // still contains "initializing"
                        TrainingScriptProgress.Creating
                    }

                    lastProgressData == TrainingScriptProgress.Initializing &&
                        progressText == "initializing" -> {
                        // It's initializing if the training thread wrote Initializing and the progress
                        // file still contains "initializing"
                        TrainingScriptProgress.Initializing
                    }

                    else -> {
                        // Otherwise, it progressed further than Initializing.
                        when (lastProgressData) {
                            // These statuses are reasonable
                            is TrainingScriptProgress.Completed -> lastProgressData
                            is TrainingScriptProgress.Error -> lastProgressData
                            else -> {
                                // Otherwise it must be InProgress
                                try {
                                    TrainingScriptProgress.InProgress(
                                        progressText.toInt() / epochs.toDouble()
                                    )
                                } catch (ex: NumberFormatException) {
                                    TrainingScriptProgress.Error(
                                        "Invalid progress text: $progressText"
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Training thread died or is not started yet. If it dies, either it finished and wrote
                // Completed to scriptProgressMap or exploded and didn't write Completed. If it is not
                // started yet, then the status will still be Creating.
                when (lastProgressData) {
                    is TrainingScriptProgress.Creating -> lastProgressData
                    is TrainingScriptProgress.Completed -> lastProgressData
                    else -> TrainingScriptProgress.Error(
                        "The training thread did not exit cleanly. " +
                            "Last progress data: $lastProgressData"
                    )
                }
            }
        }
    }

    override fun overrideTrainingProgress(jobId: Int, progress: TrainingScriptProgress) {
        overriddenProgressMap[jobId] = progress
    }

    private fun requireJobIsInMaps(jobId: Int) {
        require(jobId in scriptDataMap.keys)
        require(jobId in scriptProgressMapMap.keys)
        require(jobId in scriptThreadMap.keys)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
