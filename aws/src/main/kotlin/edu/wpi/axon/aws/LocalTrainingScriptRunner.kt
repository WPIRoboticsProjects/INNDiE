package edu.wpi.axon.aws

import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.createProgressFilePath
import edu.wpi.axon.util.runCommand
import java.io.File
import java.lang.NumberFormatException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.concurrent.thread
import mu.KotlinLogging

/**
 * Runs the training script on the local machine. Assumes that Axon is running in the
 * wpilib/axon-hosted Docker container.
 */
class LocalTrainingScriptRunner : TrainingScriptRunner {

    private val scriptDataMap = mutableMapOf<Int, RunTrainingScriptConfiguration>()
    private val scriptProgressMap = mutableMapOf<Int, TrainingScriptProgress>()
    private val scriptThreadMap = mutableMapOf<Int, Thread>()

    override fun startScript(config: RunTrainingScriptConfiguration) {
        require(config.oldModelName is FilePath.Local) {
            "Must start from a local model. Got: ${config.oldModelName}"
        }
        require(config.newModelName is FilePath.Local) {
            "Must export to a local model. Got: ${config.newModelName}"
        }
        require(config.epochs > 0) {
            "Must train for at least one epoch. Got ${config.epochs} epochs."
        }
        when (config.dataset) {
            is Dataset.Custom -> require(config.dataset.path is FilePath.Local) {
                "Custom datasets must be local. Got non-local dataset: ${config.dataset}"
            }
        }

        val scriptFile = Files.createTempFile("", ".py").toFile().apply {
            createNewFile()
            writeText(config.scriptContents)
        }

        // Clear the progress file if there was a previous run
        File(createProgressFilePath(config.id)).apply {
            parentFile.mkdirs()
            createNewFile()
            writeText("initializing")
        }

        scriptDataMap[config.id] = config
        scriptProgressMap[config.id] = TrainingScriptProgress.Creating
        scriptThreadMap[config.id] = thread {
            scriptProgressMap[config.id] = TrainingScriptProgress.Initializing

            runCommand(
                listOf("python3.6", scriptFile.absolutePath),
                emptyMap(),
                null
            ).attempt().unsafeRunSync().fold(
                {
                    LOGGER.debug(it) { "Training script failed." }
                    scriptProgressMap[config.id] = TrainingScriptProgress.Error
                },
                { (exitCode, stdOut, stdErr) ->
                    LOGGER.debug {
                        """
                        |Training script completed.
                        |Process exit code: $exitCode
                        |Process std out:
                        |$stdOut
                        |
                        |Process std err:
                        |$stdErr
                        |
                        """.trimMargin()
                    }

                    val newModelFile =
                        Paths.get(config.newModelName.path).toFile()
                    if (newModelFile.exists()) {
                        scriptProgressMap[config.id] = TrainingScriptProgress.Completed
                    } else {
                        scriptProgressMap[config.id] = TrainingScriptProgress.Error
                    }
                }
            )
        }
    }

    override fun getTrainingProgress(jobId: Int): TrainingScriptProgress {
        requireJobIsInMaps(jobId)
        val lastProgressData = scriptProgressMap[jobId]!!
        return if (scriptThreadMap[jobId]!!.isAlive) {
            // Training thread is still running. Try to read the progress file.
            val progressFile = File(createProgressFilePath(jobId))
            val progressText = progressFile.readText()

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
                                    progressText.toDouble() / scriptDataMap[jobId]!!.epochs
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

    override fun cancelScript(jobId: Int) {
        requireJobIsInMaps(jobId)
        scriptThreadMap[jobId]?.interrupt()
        scriptProgressMap[jobId] = TrainingScriptProgress.Error
    }

    private fun requireJobIsInMaps(jobId: Int) {
        require(jobId in scriptDataMap.keys)
        require(jobId in scriptThreadMap.keys)
        require(jobId in scriptProgressMap.keys)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
