package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.createLocalProgressFilepath
import edu.wpi.axon.util.runCommand
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.concurrent.thread
import mu.KotlinLogging

/**
 * Runs the training script on the local machine. Assumes that Axon is running in the
 * wpilib/axon-hosted Docker container.
 *
 * @param progressReportingDirPrefix The prefix for the local progress reporting directory.
 */
class LocalTrainingScriptRunner(
    private val progressReportingDirPrefix: String = "/tmp/progress_reporting"
) : TrainingScriptRunner {

    private val scriptDataMap = mutableMapOf<Int, RunTrainingScriptConfiguration>()
    private val scriptProgressMap = mutableMapOf<Int, TrainingScriptProgress>()
    private val scriptThreadMap = mutableMapOf<Int, Thread>()
    private val progressReporter = LocalTrainingScriptProgressReporter(progressReportingDirPrefix)
    private val canceller = LocalTrainingScriptCanceller()

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
        File(createLocalProgressFilepath(progressReportingDirPrefix, config.id)).apply {
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

        progressReporter.addJob(config, scriptProgressMap, scriptThreadMap[config.id]!!)
        canceller.addJob(config.id, scriptThreadMap[config.id]!!) {
            scriptProgressMap[config.id] = it
        }
    }

    override fun getTrainingProgress(jobId: Int) = progressReporter.getTrainingProgress(jobId)

    override fun overrideTrainingProgress(jobId: Int, progress: TrainingScriptProgress) =
        progressReporter.overrideTrainingProgress(jobId, progress)

    override fun cancelScript(jobId: Int) = canceller.cancelScript(jobId)

    private fun requireJobIsInMaps(jobId: Int) {
        require(jobId in scriptDataMap.keys)
        require(jobId in scriptThreadMap.keys)
        require(jobId in scriptProgressMap.keys)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
