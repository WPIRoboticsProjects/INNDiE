package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.createLocalProgressFilepath
import edu.wpi.axon.util.getOutputModelName
import edu.wpi.axon.util.runCommand
import java.io.File
import java.nio.file.Paths
import kotlin.concurrent.thread
import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils

/**
 * Runs the training script on the local machine. All the files the scripts loads must be in the
 * [RunTrainingScriptConfiguration.workingDir], with the exception of the model and the dataset,
 * which must at their respective [FilePath.Local.path].
 */
class LocalTrainingScriptRunner : TrainingScriptRunner {

    private val scriptDataMap = mutableMapOf<Int, RunTrainingScriptConfiguration>()
    private val scriptProgressMap = mutableMapOf<Int, TrainingScriptProgress>()
    private val scriptThreadMap = mutableMapOf<Int, Thread>()
    private val progressReporter = LocalTrainingScriptProgressReporter()
    private val canceller = LocalTrainingScriptCanceller()
    private val resultSupplier = LocalTrainingResultSupplier()

    override fun startScript(config: RunTrainingScriptConfiguration) {
        val oldModelName = config.oldModelName
        require(oldModelName is FilePath.Local) {
            "Must start from a local model. Got: $oldModelName"
        }
        require(config.epochs > 0) {
            "Must train for at least one epoch. Got ${config.epochs} epochs."
        }
        when (config.dataset) {
            is Dataset.Custom -> require(config.dataset.path is FilePath.Local) {
                "Custom datasets must be local. Got non-local dataset: ${config.dataset}"
            }
        }

        val scriptFilename = "${RandomStringUtils.randomAlphanumeric(20)}.py"
        val scriptPath = "${config.workingDir}/$scriptFilename"
        File(scriptPath).apply {
            createNewFile()
            writeText(
                config.scriptContents
                    // Remap the working dir into the "current" dir
                    .replace(
                        config.workingDir.toString(),
                        "."
                    )
                    // Remap the old model path from wherever it is on the local disk into the
                    // /models directory in the container
                    .replace(
                        oldModelName.path,
                        "/models/${oldModelName.filename}"
                    )
                    .let {
                        if (config.dataset is Dataset.Custom &&
                            config.dataset.path is FilePath.Local
                        ) {
                            // Remap the custom dataset path int o the /datasets dir if there is a
                            // custom dataset
                            it.replace(
                                config.dataset.path.path,
                                "/datasets/${config.dataset.path.filename}"
                            )
                        } else it
                    }
            )
        }

        // Clear the progress file if there was a previous run
        createLocalProgressFilepath(config.workingDir).toFile().apply {
            parentFile.mkdirs()
            createNewFile()
            writeText("initializing")
        }

        scriptDataMap[config.id] = config
        scriptProgressMap[config.id] = TrainingScriptProgress.Creating
        scriptThreadMap[config.id] = thread {
            scriptProgressMap[config.id] = TrainingScriptProgress.Initializing

            runCommand(
                listOf(
                    "docker",
                    "run",
                    "--rm",
                    // Most of the files should be in the working dir
                    "--mount",
                    "type=bind,source=${config.workingDir},target=/home",
                    // The model is remapped in the script above
                    "--mount",
                    "type=bind,source=${Paths.get(oldModelName.path).parent},target=/models"
                ) + when (config.dataset) {
                    is Dataset.Custom -> listOf(
                        "--mount",
                        "type=bind,source=${Paths.get(config.dataset.path.path).parent},target=/datasets"
                    )
                    is Dataset.ExampleDataset -> emptyList()
                } + listOf(
                    "wpilib/axon-ci:latest",
                    "/usr/bin/python3.6",
                    "/home/$scriptFilename"
                ),
                emptyMap(),
                null
            ).attempt().unsafeRunSync().fold(
                {
                    LOGGER.debug(it) { "Training script failed." }
                    scriptProgressMap[config.id] = TrainingScriptProgress.Error(it.localizedMessage)
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

                    val newModelFile = config.workingDir
                        .resolve(getOutputModelName(oldModelName.filename))
                        .toFile()
                    if (newModelFile.exists()) {
                        scriptProgressMap[config.id] = TrainingScriptProgress.Completed
                    } else {
                        scriptProgressMap[config.id] = TrainingScriptProgress.Error(
                            "The trained model file (${newModelFile.path}) does not exist."
                        )
                    }
                }
            )
        }

        progressReporter.addJob(config, scriptProgressMap, scriptThreadMap[config.id]!!)
        canceller.addJob(config.id, scriptThreadMap[config.id]!!) {
            scriptProgressMap[config.id] = it
        }
        resultSupplier.addJob(config.id, config.workingDir)
    }

    override fun listResults(id: Int) = resultSupplier.listResults(id)

    override fun getResult(id: Int, filename: String) = resultSupplier.getResult(id, filename)

    override fun getTrainingProgress(jobId: Int) = progressReporter.getTrainingProgress(jobId)

    override fun overrideTrainingProgress(jobId: Int, progress: TrainingScriptProgress) =
        progressReporter.overrideTrainingProgress(jobId, progress)

    override fun cancelScript(jobId: Int) = canceller.cancelScript(jobId)

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
