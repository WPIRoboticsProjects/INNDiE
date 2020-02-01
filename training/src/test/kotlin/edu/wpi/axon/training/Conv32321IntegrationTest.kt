@file:SuppressWarnings("LongMethod", "LargeClass")

package edu.wpi.axon.training

import arrow.core.None
import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.testutil.loadModel
import edu.wpi.axon.training.testutil.testTrainingScript
import edu.wpi.axon.util.FilePath
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.matchers.types.shouldBeInstanceOf
import java.io.File
import kotlin.random.Random
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.context.startKoin

internal class Conv32321IntegrationTest : KoinTestFixture() {

    @Test
    @Tag("needsTensorFlowSupport")
    fun `test with sequential`(@TempDir tempDir: File) {
        startKoin {
            modules(defaultBackendModule())
        }

        val modelName = "32_32_1_conv_sequential.h5"
        val newModelName = tempDir.toPath().resolve("32_32_1_conv_sequential-trained.h5").toString()
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.Sequential> {
            TrainSequentialModelScriptGenerator(
                TrainState(
                    userOldModelPath = FilePath.Local(path),
                    userDataset = Dataset.ExampleDataset.FashionMnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 1,
                    userNewModel = it,
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
    fun `test with general`(@TempDir tempDir: File) {
        startKoin {
            modules(defaultBackendModule())
        }

        val modelName = "32_32_1_conv_general.h5"
        val newModelName = tempDir.toPath().resolve("32_32_1_conv_general-trained.h5").toString()
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.General> {
            TrainGeneralModelScriptGenerator(
                TrainState(
                    userOldModelPath = FilePath.Local(path),
                    userDataset = Dataset.ExampleDataset.FashionMnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 1,
                    userNewModel = it,
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
}
