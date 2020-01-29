package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.util.createProgressFilePath
import mu.KotlinLogging
import java.io.File
import java.lang.NumberFormatException

class LocalTrainingScriptProgressReporter : TrainingScriptProgressReporter {

    private val scriptDataMap = mutableMapOf<Int, RunTrainingScriptConfiguration>()
    private var scriptProgressMapMap = mutableMapOf<Int, Map<Int, TrainingScriptProgress>?>()
    private val scriptThreadMap = mutableMapOf<Int, Thread?>()

    fun addJob(
        config: RunTrainingScriptConfiguration,
        progressMap: Map<Int, TrainingScriptProgress>,
        thread: Thread
    ) {
        scriptDataMap[config.id] = config
        scriptProgressMapMap[config.id] = progressMap
        scriptThreadMap[config.id] = thread
    }

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
        val progressMap = scriptProgressMapMap[jobId]
        val epochs = scriptDataMap[jobId]!!.epochs

        // Create the progress file up here to share code but don't read from it unless we have to,
        // to avoid reading from it if it's not there but didn't have to be
        val progressFile = File(createProgressFilePath(jobId))

        return if (progressMap == null) {
            // We are resuming from a restart because there was no initial progress data. Remove
            // whitespace from the progress text.
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
                TrainingScriptProgress.Error
            }
        } else {
            // If progressMap wasn't null, then the thread should've been set as well
            require(scriptThreadMap[jobId] != null)

            val lastProgressData = progressMap[jobId]!!
            if (scriptThreadMap[jobId]!!.isAlive) {
                // Training thread is still running. Try to read the progress file. Remove
                // whitespace.
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
                            is TrainingScriptProgress.Completed -> TrainingScriptProgress.Completed
                            is TrainingScriptProgress.Error -> TrainingScriptProgress.Error
                            else -> {
                                // Otherwise it must be InProgress
                                try {
                                    TrainingScriptProgress.InProgress(
                                        progressText.toInt() / epochs.toDouble()
                                    )
                                } catch (ex: NumberFormatException) {
                                    TrainingScriptProgress.Error
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
                    is TrainingScriptProgress.Creating -> TrainingScriptProgress.Creating
                    is TrainingScriptProgress.Completed -> TrainingScriptProgress.Completed
                    else -> TrainingScriptProgress.Error
                }
            }
        }
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
