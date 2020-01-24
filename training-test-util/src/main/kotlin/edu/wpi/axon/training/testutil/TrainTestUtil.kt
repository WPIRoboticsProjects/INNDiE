package edu.wpi.axon.training.testutil

import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tflayerloader.ModelLoaderFactory
import edu.wpi.axon.util.runCommand
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.shouldBe
import java.io.File
import java.nio.file.Paths
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger("training-test-util")

/**
 * Loads a model with name [modelName] from the test resources.
 *
 * @param modelName The name of the model.
 * @param stub Used to get the calling class. Just supply an empty lambda.
 * @return The model and its path.
 */
fun loadModel(modelName: String, stub: () -> Unit): Pair<Model, String> {
    val localModelPath = Paths.get(stub::class.java.getResource(modelName).toURI()).toString()
    val layers = ModelLoaderFactory().createModeLoader(localModelPath).load(File(localModelPath))
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
fun testTrainingScript(
    oldModelPath: String,
    oldModelName: String,
    newModelName: String,
    script: String,
    dir: File,
    cleanupAfterCommand: String? = null
) {
    Paths.get(oldModelPath).toFile().copyTo(Paths.get(dir.absolutePath, oldModelName).toFile())
    Paths.get(dir.absolutePath, "script.py").toFile().writeText(script)

    runCommand(
        listOf(
            "docker",
            "run",
            "--rm",
            "-v",
            "${dir.absolutePath}:/home",
            "wpilib/axon-ci:latest",
            cleanupAfterCommand?.let {
                "/usr/bin/python3.6 /home/script.py && $it"
            } ?: "/usr/bin/python3.6 /home/script.py"
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

        stdOut.shouldNotBeEmpty()
        exitCode shouldBe 0
        Paths.get(dir.absolutePath, newModelName).shouldExist()
    }
}
