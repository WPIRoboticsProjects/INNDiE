package edu.wpi.axon.aws

import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.training.ModelPath
import edu.wpi.axon.util.runCommand
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import mu.KotlinLogging

/**
 * Runs the training script on the local machine. Assumes that Axon is running in the
 * wpilib/axon-hosted Docker container.
 */
class LocalTrainingScriptRunner : TrainingScriptRunner {

    private val nextScriptId = AtomicLong()
    private val scriptDataMap = mutableMapOf<Long, RunTrainingScriptConfiguration>()
    private val scriptProgressMap = mutableMapOf<Long, TrainingScriptProgress>()
    private val scriptThreadMap = mutableMapOf<Long, Thread>()

    override fun startScript(runTrainingScriptConfiguration: RunTrainingScriptConfiguration): Long {
        require(runTrainingScriptConfiguration.oldModelName is ModelPath.Local)
        require(runTrainingScriptConfiguration.newModelName is ModelPath.Local)
        // TODO: Ensure the dataset is local as well

        val scriptFile = Files.createTempFile("", ".py").toFile()
        scriptFile.createNewFile()
        scriptFile.writeText(runTrainingScriptConfiguration.scriptContents)

        val modelName = runTrainingScriptConfiguration.newModelName.filename
        val datasetName = runTrainingScriptConfiguration.dataset.nameForS3ProgressReporting
        val progressFile = File(createProgressFilePath(modelName, datasetName))
        if (!progressFile.exists()) {
            // Ensure the progress file exists so that we can ensure it has no progress in it
            check(progressFile.createNewFile()) {
                "Failed to create the progress file at: ${progressFile.absolutePath}"
            }
        }
        // Clear the progress file if there was a previous run
        progressFile.writeText("0.0")

        val scriptId = nextScriptId.getAndIncrement()
        scriptDataMap[scriptId] = runTrainingScriptConfiguration
        scriptProgressMap[scriptId] = TrainingScriptProgress.NotStarted

        scriptThreadMap[scriptId] = thread {
            scriptProgressMap[scriptId] = TrainingScriptProgress.Initializing

            runCommand(
                listOf("python3.6", scriptFile.absolutePath),
                emptyMap(),
                null
            ).attempt().unsafeRunSync().fold(
                {
                    LOGGER.warn(it) { "Training script failed." }
                    scriptProgressMap[scriptId] = TrainingScriptProgress.Error
                },
                { (exitCode, stdOut, stdErr) ->
                    LOGGER.info {
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
                        Paths.get(runTrainingScriptConfiguration.newModelName.path).toFile()
                    if (newModelFile.exists()) {
                        scriptProgressMap[scriptId] = TrainingScriptProgress.Completed
                    } else {
                        scriptProgressMap[scriptId] = TrainingScriptProgress.Error
                    }
                }
            )
        }

        return scriptId
    }

    override fun getTrainingProgress(scriptId: Long): TrainingScriptProgress {
        require(scriptId in scriptDataMap.keys)
        require(scriptId in scriptThreadMap.keys)
        require(scriptId in scriptProgressMap.keys)

        return if (scriptThreadMap[scriptId]!!.isAlive) {
            // Training thread is still running. Try to read the progress file.
            val config = scriptDataMap[scriptId]!!
            val modelName = config.newModelName.filename
            val datasetName = config.dataset.nameForS3ProgressReporting
            val progressFile = File(createProgressFilePath(modelName, datasetName))
            if (progressFile.exists()) {
                TrainingScriptProgress.InProgress(
                    progressFile.readText().toDouble() / config.epochs
                )
            } else {
                TrainingScriptProgress.InProgress(0.0)
            }
        } else {
            // Training thread died. Either it finished and wrote Completed to scriptProgressMap
            // or exploded and didn't write Completed.
            when (scriptProgressMap[scriptId]!!) {
                is TrainingScriptProgress.Completed -> TrainingScriptProgress.Completed
                else -> TrainingScriptProgress.Error
            }
        }
    }

    private fun createProgressFilePath(modelName: String, datasetName: String) =
        "/tmp/progress_reporting/$modelName/$datasetName/progress.txt"

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
