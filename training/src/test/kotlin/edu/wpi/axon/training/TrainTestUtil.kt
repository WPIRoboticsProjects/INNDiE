package edu.wpi.axon.training

import arrow.core.Tuple3
import arrow.fx.IO
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.shouldBe
import mu.KotlinLogging
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths

private val LOGGER = KotlinLogging.logger("training-test-util")

/**
 * Loads a model with name [modelName] from the test resources.
 *
 * @param modelName The name of the model.
 * @param stub Used to get the calling class. Do not use this parameter.
 * @return The model and its path.
 */
internal fun loadModel(modelName: String, stub: () -> Unit = {}): Pair<Model, String> {
    val localModelPath = Paths.get(stub::class.java.getResource(modelName).toURI()).toString()
    val layers = LoadLayersFromHDF5(DefaultLayersToGraph())
        .load(File(localModelPath))
    val model = layers.attempt().unsafeRunSync()
    model.shouldBeRight()
    return model.b as Model to localModelPath
}

/**
 * Tests that a training scripts works by running it in the axon-ci Docker container and asserting
 * that the expected trained model file is written to disk.
 *
 * @param oldModelPath The path to load the old model from.
 * @param oldModelName The name of the old model.
 * @param newModelName The name of the new model.
 * @param script The content of the script to run.
 * @param dir The working directory.
 */
internal fun testTrainingScript(
    oldModelPath: String,
    oldModelName: String,
    newModelName: String,
    script: String,
    dir: File
) {
    Paths.get(oldModelPath).toFile().copyTo(Paths.get(dir.absolutePath, oldModelName).toFile())
    Paths.get(dir.absolutePath, "script.py").toFile().writeText(script)

    runCommand(
        listOf(
            "docker",
            "run",
            "--rm",
            "wpilib/axon-ci:latest",
            "/usr/bin/python3.6",
            "script.py",
            "&&",
            "test",
            "-f",
            newModelName
        ),
        emptyMap(),
        dir
    ).attempt().unsafeRunSync().shouldBeRight { (exitCode, stdOut, stdErr) ->
        LOGGER.info {
            """
            |Process std out:
            |$stdOut
            |
            |Process std err:
            |$stdErr
            |
            """.trimMargin()
        }

        exitCode shouldBe 0
    }
}

/**
 * Runs a command as a process.
 *
 * @param command The command and its arguments.
 * @param env Any extra env vars.
 * @param dir The working directory of the process.
 * @return The exit code, std out, and std err.
 */
@Suppress("BlockingMethodInNonBlockingContext")
internal fun runCommand(
    command: List<String>,
    env: Map<String, String>,
    dir: File
): IO<Tuple3<Int, String, String>> = IO {
    val proc = ProcessBuilder(command)
        .directory(dir)
        .also {
            val builderEnv = it.environment()
            env.forEach { (key, value) ->
                builderEnv[key] = value
            }
        }.start()

    BufferedReader(InputStreamReader(proc.inputStream)).useLines { procStdOut ->
        BufferedReader(InputStreamReader(proc.errorStream)).useLines { procStdErr ->
            val exitCode = proc.waitFor()
            Tuple3(
                exitCode,
                procStdOut.joinToString("\n"),
                procStdErr.joinToString("\n")
            )
        }
    }
}
