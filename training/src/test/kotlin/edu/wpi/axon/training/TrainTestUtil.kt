package edu.wpi.axon.training

import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import io.kotlintest.assertions.arrow.either.shouldBeRight
import java.io.File

// TODO: Make these dev-configurable without requiring code changes
internal fun getTestBucketName() = "axon-salmon-testbucket1"
internal fun getTestRegion() = "us-east-1"

/**
 * Loads a model with name [modelName] from the test resources.
 *
 * @param modelName The name of the model.
 * @param stub Used to get the calling class. Do not use this parameter.
 * @return The model and its path.
 */
internal fun loadModel(modelName: String, stub: () -> Unit = {}): Pair<Model, String> {
    val localModelPath = stub::class.java.getResource(modelName).toURI().path
    val layers = LoadLayersFromHDF5(DefaultLayersToGraph())
        .load(File(localModelPath))
    val model = layers.attempt().unsafeRunSync()
    model.shouldBeRight()
    return model.b as Model to localModelPath
}
