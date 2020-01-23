@file:SuppressWarnings("LongMethod", "LargeClass")

package edu.wpi.axon.training

import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.testutil.loadModel
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class TrainGeneralModelScriptGeneratorIntegrationTest : KoinTestFixture() {

    @Test
    fun `test with custom model with an add`() {
        startKoin {
            modules(defaultBackendModule())
        }

        val modelName = "network_with_add.h5"
        val newModelName = "network_with_add-trained.h5"
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.General> {
            TrainGeneralModelScriptGenerator(
                TrainState(
                    userOldModelPath = path,
                    userNewModelName = newModelName,
                    userDataset = Dataset.ExampleDataset.Mnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 50,
                    userNewModel = it
                )
            ).generateScript().shouldBeValid()
        }
    }
}
