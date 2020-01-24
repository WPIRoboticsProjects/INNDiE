@file:SuppressWarnings("LongMethod", "LargeClass")

package edu.wpi.axon.training

import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.testutil.loadModel
import edu.wpi.axon.training.testutil.testTrainingScript
import edu.wpi.axon.util.axonBucketName
import io.kotlintest.assertions.arrow.validation.shouldBeInvalid
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.matchers.types.shouldBeInstanceOf
import java.io.File
import java.nio.file.Paths
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal class TrainSequentialModelScriptGeneratorIntegrationTest : KoinTestFixture() {

    @Test
    fun `test with bad model`() {
        startKoin {
            modules(defaultBackendModule())
        }

        val localModelPath =
            Paths.get(this::class.java.getResource("badModel1.h5").toURI()).toString()
        TrainSequentialModelScriptGenerator(
            TrainState(
                userOldModelPath = ModelPath.Local(localModelPath),
                userNewModelPath = ModelPath.Local("/tmp/badModel1-trained.h5"),
                userDataset = Dataset.ExampleDataset.Mnist,
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
            modules(
                listOf(
                    defaultBackendModule(),
                    module {
                        single(named(axonBucketName)) { "dummy-bucket-name" }
                    }
                )
            )
        }

        val modelName = "custom_fashion_mnist.h5"
        // Use the current directory because this runs in a new container
        val newModelName = "custom_fashion_mnist-trained.h5"
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.Sequential> {
            TrainSequentialModelScriptGenerator(
                TrainState(
                    userOldModelPath = ModelPath.Local(path),
                    userNewModelPath = ModelPath.Local(newModelName),
                    userDataset = Dataset.ExampleDataset.Mnist,
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
                testTrainingScript(
                    path,
                    modelName,
                    newModelName,
                    // Patch the script because it's not meant to run in a container
                    script.a.replace(path, modelName),
                    tempDir
                )
            }
        }
    }

    @Test
    @Tag("needsDockerSupport")
    fun `test mobilenet with reduced wpilib dataset`(@TempDir tempDir: File) {
        startKoin {
            modules(defaultBackendModule())
        }

        val modelName = "small_model_for_wpilib_reduced_dataset.h5"
        // Use the current directory because this runs in a new container
        val newModelName = "small_model_for_wpilib_reduced_dataset-trained.h5"
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.Sequential> {
            TrainSequentialModelScriptGenerator(
                TrainState(
                    userOldModelPath = ModelPath.Local(path),
                    userNewModelPath = ModelPath.Local(newModelName),
                    userDataset = Dataset.Custom("WPILib_reduced.tar", "WPILib reduced"),
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 1,
                    userNewModel = it.copy(
                        layers = it.layers.mapIndexedTo(mutableSetOf()) { index, layer ->
                            // Only train the last layer
                            if (it.layers.size - index <= 1) layer.layer.trainable()
                            else layer.layer.trainable(false)
                        })
                )
            ).generateScript()
                .shouldBeValid { script ->
                    Paths.get(this::class.java.getResource("WPILib_reduced.tar").toURI()).toFile()
                        .copyTo(Paths.get(tempDir.absolutePath, "WPILib_reduced.tar").toFile())
                    testTrainingScript(
                        path,
                        modelName,
                        newModelName,
                        // Patch the script because it's not meant to run in a container
                        script.a.replace(path, modelName),
                        tempDir,
                        "rm -rf /home/WPILib_reduced"
                    )
                }
        }
    }
}
