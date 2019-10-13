package edu.wpi.axon.tflayerloader

import edu.wpi.axon.tfdata.Model
import io.kotlintest.assertions.arrow.either.shouldBeLeft
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.types.shouldBeInstanceOf
import java.io.File

/**
 * Loads a model from a file and asserts that it loaded successfully. Runs the [block] with the
 * loaded model.
 *
 * @param filename The filename of the model.
 * @param block Will be run with the loaded model.
 */
internal inline fun <reified T : Model> loadModel(filename: String, noinline block: (T) -> Unit) {
    LoadLayersFromHDF5(DefaultLayersToGraph()).load(
        File(block::class.java.getResource(filename).toURI())
    ).attempt().unsafeRunSync().shouldBeRight { model ->
        model.shouldBeInstanceOf(block)
    }
}

/**
 * Asserts that loading the model from a file fails.
 *
 * @param filename The filename of the model.
 * @param stub Used to get the class to get a resource from. Do not use this parameter.
 */
fun loadModelFails(filename: String, stub: () -> Unit = {}) {
    LoadLayersFromHDF5(DefaultLayersToGraph()).load(
        File(stub::class.java.getResource(filename).toURI())
    ).attempt().unsafeRunSync().shouldBeLeft()
}
