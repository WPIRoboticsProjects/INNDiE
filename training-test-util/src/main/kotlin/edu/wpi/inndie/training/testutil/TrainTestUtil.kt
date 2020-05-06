package edu.wpi.inndie.training.testutil

import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tflayerloader.ModelLoaderFactory
import edu.wpi.inndie.util.runCommand
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.shouldBe
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Loads a model with name [modelName] from the test resources.
 *
 * @param modelName The name of the model.
 * @param stub Used to get the calling class. Just supply an empty lambda.
 * @return The model and its path.
 */
fun loadModel(modelName: String, stub: () -> Unit): Pair<Model, String> {
    val localModelPath = Paths.get(stub::class.java.getResource(modelName).toURI()).toString()
    val layers = ModelLoaderFactory().createModelLoader(localModelPath).load(File(localModelPath))
    val model = layers.attempt().unsafeRunSync()
    model.shouldBeRight()
    return model.b as Model to localModelPath
}

/**
 * Tests that a training scripts works by running it and asserting that the trained model file was
 * written to disk.
 *
 * @param dir The working directory to run the script in.
 * @param script The content of the script to run.
 * @param newModelName The name of the new model.
 */
fun testTrainingScript(dir: File, script: String, newModelName: String) {
    val scriptFile = Files.createTempFile(dir.toPath(), "", ".py").toFile()
    scriptFile.writeText(script)
    runCommand(
        listOf("python3.6", scriptFile.path),
        emptyMap(),
        dir
    ).attempt().unsafeRunSync().shouldBeRight { (exitCode, stdOut, stdErr) ->
        println(
            """
            |Process std out:
            |$stdOut
            |
            |Process std err:
            |$stdErr
            |
            """.trimMargin()
        )

        exitCode shouldBe 0
        Paths.get(newModelName).shouldExist()
    }
}
