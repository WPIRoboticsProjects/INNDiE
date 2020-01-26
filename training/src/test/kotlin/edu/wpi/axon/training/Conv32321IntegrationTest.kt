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
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.matchers.types.shouldBeInstanceOf
import java.io.File
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.context.startKoin

internal class Conv32321IntegrationTest : KoinTestFixture() {

    @Test
    @Tag("needsDockerSupport")
    fun `test with sequential`(@TempDir tempDir: File) {
        startKoin {
            modules(defaultBackendModule())
        }

        val modelName = "32_32_1_conv_sequential.h5"
        val newModelName = "/tmp/32_32_1_conv_sequential-trained.h5"
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.Sequential> {
            TrainSequentialModelScriptGenerator(
                TrainState(
                    userOldModelPath = ModelPath.Local("./$modelName"),
                    userNewModelPath = ModelPath.Local(newModelName),
                    userDataset = Dataset.ExampleDataset.FashionMnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 1,
                    userNewModel = it
                )
            ).generateScript().shouldBeValid { (script) ->
                testTrainingScript(path, modelName, newModelName, script, tempDir)
            }
        }
    }

    @Test
    @Tag("needsDockerSupport")
    fun `test with general`(@TempDir tempDir: File) {
        startKoin {
            modules(defaultBackendModule())
        }

        val modelName = "32_32_1_conv_general.h5"
        val newModelName = "/tmp/32_32_1_conv_general-trained.h5"
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.General> {
            TrainGeneralModelScriptGenerator(
                TrainState(
                    userOldModelPath = ModelPath.Local("./$modelName"),
                    userNewModelPath = ModelPath.Local(newModelName),
                    userDataset = Dataset.ExampleDataset.FashionMnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 1,
                    userNewModel = it
                )
            ).generateScript().shouldBeValid { (script) ->
                testTrainingScript(path, modelName, newModelName, script, tempDir)
            }
        }
    }
}
