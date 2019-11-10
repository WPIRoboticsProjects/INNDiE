@file:SuppressWarnings("LongMethod", "LargeClass")

package edu.wpi.axon.training

import edu.wpi.axon.dsl.defaultModule
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import io.kotlintest.assertions.arrow.validation.shouldBeInvalid
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.context.startKoin
import java.io.File
import java.nio.file.Paths

internal class TrainSequentialIntegrationTest : KoinTestFixture() {

    @Test
    fun `test with bad model`() {
        startKoin {
            modules(defaultModule())
        }

        val localModelPath =
            Paths.get(this::class.java.getResource("badModel1.h5").toURI()).toString()
        TrainSequential(
            TrainState(
                userOldModelPath = localModelPath,
                userNewModelName = "badModel1-trained.h5",
                userDataset = Dataset.Mnist,
                userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                userLoss = Loss.SparseCategoricalCrossentropy,
                userMetrics = setOf("accuracy"),
                userEpochs = 50,
                userNewModel = Model.Sequential(
                    "",
                    emptyList(),
                    emptySet()
                )
            )
        ).generateScript().shouldBeInvalid()
    }

    @Test
    @Tag("needsDockerSupport")
    fun `test with fashion mnist`(@TempDir tempDir: File) {
        startKoin {
            modules(defaultModule())
        }

        val modelName = "custom_fashion_mnist.h5"
        val newModelName = "custom_fashion_mnist-trained.h5"
        val (model, path) = loadModel(modelName)
        model.shouldBeInstanceOf<Model.Sequential> {
            TrainSequential(
                TrainState(
                    userOldModelPath = path,
                    userNewModelName = newModelName,
                    userDataset = Dataset.Mnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 1,
                    userNewModel = it.copy(
                        layers = it.layers.mapIndexedTo(mutableSetOf()) { index, layer ->
                            // Only train the last 3 layers
                            if (it.layers.size - index <= 3) layer.layer.trainable()
                            else layer.layer.trainable(false)
                        })
                )
            ).generateScript().shouldBeValid { script ->
                testTrainingScript(path, modelName, newModelName, script.a, tempDir)
            }
        }
    }
}
