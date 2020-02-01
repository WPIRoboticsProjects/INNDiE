@file:SuppressWarnings("LongMethod", "LargeClass")

package edu.wpi.axon.training

import arrow.core.None
import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.dsl.task.RunEdgeTpuCompilerTask
import edu.wpi.axon.plugin.DatasetPlugins
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.testutil.loadModel
import edu.wpi.axon.training.testutil.testTrainingScript
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.axonBucketName
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.matchers.types.shouldBeInstanceOf
import java.io.File
import java.nio.file.Paths
import kotlin.random.Random
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal class TrainSequentialModelScriptGeneratorIntegrationTest : KoinTestFixture() {

    @Test
    @Tag("needsTensorFlowSupport")
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
        val newModelName = tempDir.toPath().resolve("custom_fashion_mnist-trained.h5").toString()
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.Sequential> {
            TrainSequentialModelScriptGenerator(
                TrainState(
                    userOldModelPath = FilePath.Local(path),
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
                        }),
                    userValidationSplit = None,
                    generateDebugComments = false,
                    target = ModelDeploymentTarget.Desktop,
                    datasetPlugin = Plugin.Unofficial(
                        "",
                        """
                        |def process_dataset(x, y):
                        |    newX = tf.cast(x / 255.0, tf.float32)
                        |    newY = tf.cast(y / 255.0, tf.float32)
                        |    newX = newX[..., tf.newaxis]
                        |    newY = newY[..., tf.newaxis]
                        |    return (newX, newY)
                        """.trimMargin()
                    ),
                    workingDir = tempDir.toPath(),
                    jobId = Random.nextInt(1, Int.MAX_VALUE)
                ),
                it
            ).generateScript().shouldBeValid { (script) ->
                testTrainingScript(tempDir, script, newModelName)
            }
        }
    }

    @Test
    @Tag("needsTensorFlowSupport")
    fun `test with fashion mnist targeting the coral`(@TempDir tempDir: File) {
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
        val newModelName = tempDir.toPath().resolve("custom_fashion_mnist-trained.h5").toString()
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.Sequential> {
            TrainSequentialModelScriptGenerator(
                TrainState(
                    userOldModelPath = FilePath.Local(path),
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
                        }),
                    userValidationSplit = None,
                    generateDebugComments = false,
                    target = ModelDeploymentTarget.Desktop,
                    workingDir = tempDir.toPath(),
                    datasetPlugin = Plugin.Unofficial(
                        "",
                        """
                        |def process_dataset(x, y):
                        |    newX = tf.cast(x / 255.0, tf.float32)
                        |    newY = tf.cast(y / 255.0, tf.float32)
                        |    newX = newX[..., tf.newaxis]
                        |    newY = newY[..., tf.newaxis]
                        |    return (newX, newY)
                        """.trimMargin()
                    ),
                    jobId = Random.nextInt(1, Int.MAX_VALUE)
                ),
                it
            ).generateScript().shouldBeValid { (script) ->
                testTrainingScript(tempDir, script, newModelName)
            }
        }
    }

    @Test
    @Tag("needsTensorFlowSupport")
    fun `test small model with reduced wpilib dataset`(@TempDir tempDir: File) {
        startKoin {
            modules(defaultBackendModule())
        }

        // TODO: This breaks at runtime with a Coral target
        val modelName = "small_model_for_wpilib_reduced_dataset.h5"
        val newModelName = tempDir.toPath().resolve("small_model_for_wpilib_reduced_dataset-trained.h5").toString()
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.Sequential> {
            TrainSequentialModelScriptGenerator(
                TrainState(
                    userOldModelPath = FilePath.Local(path),
                    userDataset = Dataset.Custom(
                        FilePath.Local("WPILib_reduced.tar"),
                        "WPILib reduced"
                    ),
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 1,
                    userNewModel = it.copy(
                        layers = it.layers.mapIndexedTo(mutableSetOf()) { index, layer ->
                            // Only train the last layer
                            if (it.layers.size - index <= 1) layer.layer.trainable()
                            else layer.layer.trainable(false)
                        }),
                    userValidationSplit = None,
                    generateDebugComments = false,
                    target = ModelDeploymentTarget.Desktop,
                    workingDir = tempDir.toPath(),
                    datasetPlugin = Plugin.Unofficial(
                        "",
                        """
                        |def process_dataset(x, y):
                        |    newX = tf.cast(x / 255.0, tf.float32)
                        |    newY = tf.cast(y / 255.0, tf.float32)
                        |    newX = newX[..., tf.newaxis]
                        |    newY = newY[..., tf.newaxis]
                        |    return (newX, newY)
                        """.trimMargin()
                    ),
                    jobId = Random.nextInt(1, Int.MAX_VALUE)
                ),
                it
            ).generateScript().shouldBeValid { (script) ->
                Paths.get(this::class.java.getResource("WPILib_reduced.tar").toURI()).toFile()
                    .copyTo(Paths.get(tempDir.absolutePath, "WPILib_reduced.tar").toFile())
                testTrainingScript(tempDir, script, newModelName)
                // Also test for the compiled output
                Paths.get(
                    "$tempDir/" +
                        RunEdgeTpuCompilerTask.getEdgeTpuCompiledModelFilename(newModelName)
                ).shouldExist()
            }
        }
    }

    @Test
    @Tag("needsTensorFlowSupport")
    @Disabled("Broken targeting the Coral.")
    fun `test small model with reduced wpilib dataset targeting the coral`(@TempDir tempDir: File) {
        startKoin {
            modules(defaultBackendModule())
        }

        val modelName = "small_model_for_wpilib_reduced_dataset.h5"
        val newModelName = "$tempDir/small_model_for_wpilib_reduced_dataset-trained.h5"
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.Sequential> {
            TrainSequentialModelScriptGenerator(
                TrainState(
                    userOldModelPath = FilePath.Local(path),
                    userDataset = Dataset.Custom(
                        FilePath.Local("WPILib_reduced.tar"),
                        "WPILib reduced"
                    ),
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 1,
                    userNewModel = it.copy(
                        layers = it.layers.mapIndexedTo(mutableSetOf()) { index, layer ->
                            // Only train the last layer
                            if (it.layers.size - index <= 1) layer.layer.trainable()
                            else layer.layer.trainable(false)
                        }),
                    userValidationSplit = None,
                    generateDebugComments = false,
                    target = ModelDeploymentTarget.Coral(0.0001),
                    workingDir = tempDir.toPath(),
                    datasetPlugin = DatasetPlugins.datasetPassthroughPlugin,
                    jobId = Random.nextInt(1, Int.MAX_VALUE)
                ),
                it
            ).generateScript().shouldBeValid { (script) ->
                Paths.get(this::class.java.getResource("WPILib_reduced.tar").toURI()).toFile()
                    .copyTo(Paths.get(tempDir.absolutePath, "WPILib_reduced.tar").toFile())
                testTrainingScript(tempDir, script, newModelName)
            }
        }
    }
}
