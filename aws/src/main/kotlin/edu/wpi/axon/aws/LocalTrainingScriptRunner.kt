package edu.wpi.axon.aws

import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.training.ModelPath
import edu.wpi.axon.util.runCommand
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
            // Training thread is still running.
            // TODO: Measure the actual progress here
            TrainingScriptProgress.InProgress(0.0)
        } else {
            // Training thread died. Either it finished and wrote Completed to scriptProgressMap
            // or exploded and didn't write Completed.
            when (scriptProgressMap[scriptId]!!) {
                is TrainingScriptProgress.Completed -> TrainingScriptProgress.Completed
                else -> TrainingScriptProgress.Error
            }
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
