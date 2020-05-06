@file:SuppressWarnings("LongMethod", "LargeClass")

package edu.wpi.inndie.training

import arrow.core.None
import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.plugin.DatasetPlugins.processMnistTypePlugin
import edu.wpi.inndie.testutil.KoinTestFixture
import edu.wpi.inndie.tfdata.Dataset
import edu.wpi.inndie.tfdata.Model
import edu.wpi.inndie.tfdata.loss.Loss
import edu.wpi.inndie.tfdata.optimizer.Optimizer
import edu.wpi.inndie.training.testutil.loadModel
import edu.wpi.inndie.training.testutil.testTrainingScript
import edu.wpi.inndie.util.FilePath
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
                    datasetPlugin = processMnistTypePlugin,
                    jobId = Random.nextInt(1, Int.MAX_VALUE)
                ),
                it
            ).generateScript().shouldBeValid { (script) ->
                testTrainingScript(
                    tempDir,
                    script,
                    newModelName
                )
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
                    datasetPlugin = processMnistTypePlugin,
                    jobId = Random.nextInt(1, Int.MAX_VALUE)
                ),
                it
            ).generateScript().shouldBeValid { (script) ->
                testTrainingScript(
                    tempDir,
                    script,
                    newModelName
                )
            }
        }
    }
}
