@file:SuppressWarnings("LongMethod", "LargeClass")

package edu.wpi.axon.training

import edu.wpi.axon.dsl.defaultModule
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.context.startKoin
import java.io.File

internal class TrainGeneralIntegrationTest : KoinTestFixture() {

    @Test
    fun `test with custom model with an add`(@TempDir tempDir: File) {
        startKoin {
            modules(defaultModule())
        }

        val modelName = "network_with_add.h5"
        val newModelName = "network_with_add-trained.h5"
        val (model, path) = loadModel(modelName)
        model.shouldBeInstanceOf<Model.General> {
            TrainGeneral(
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
