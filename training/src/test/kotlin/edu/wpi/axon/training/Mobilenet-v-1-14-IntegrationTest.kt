@file:SuppressWarnings("LongMethod", "LargeClass")

package edu.wpi.axon.training

import arrow.core.None
import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.testutil.loadModel
import edu.wpi.axon.util.FilePath
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.matchers.types.shouldBeInstanceOf
import java.nio.file.Path
import kotlin.random.Random
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class `Mobilenet-v-1-14-IntegrationTest` : KoinTestFixture() {

    @Test
    fun `test with mobilenet`() {
        startKoin {
            modules(defaultBackendModule())
        }

        // TODO: Use a dataset that works with this model so we can actually run the training script

        val modelName = "mobilenetv2_1.00_224.h5"
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.General> {
            TrainGeneralModelScriptGenerator(
                TrainState(
                    userOldModelPath = FilePath.Local(path),
                    userDataset = Dataset.ExampleDataset.Mnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 50,
                    userNewModel = it,
                    userValidationSplit = None,
                    generateDebugComments = false,
                    target = ModelDeploymentTarget.Desktop,
                    workingDir = Path.of("."),
                    jobId = Random.nextInt(1, Int.MAX_VALUE)
                ),
                it
            ).generateScript().shouldBeValid()
        }
    }
}
