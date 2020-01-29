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
import io.kotlintest.assertions.arrow.validation.shouldBeInvalid
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import java.nio.file.Paths
import kotlin.random.Random
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class TrainIntegrationTest : KoinTestFixture() {

    @Test
    fun `test general model`() {
        startKoin {
            modules(defaultBackendModule())
        }

        val (model, path) = loadModel("network_with_add.h5") {}
        model.shouldBeInstanceOf<Model.General> {
            TrainGeneralModelScriptGenerator(
                TrainState(
                    userOldModelPath = FilePath.Local(path),
                    userNewModelPath = FilePath.Local("/tmp/network_with_add-trained.h5"),
                    userDataset = Dataset.ExampleDataset.Mnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 50,
                    userNewModel = it,
                    userValidationSplit = None,
                    generateDebugComments = false,
                    target = ModelDeploymentTarget.Desktop,
                    jobId = Random.nextInt(1, Int.MAX_VALUE)
                ),
                it
            ).generateScript().shouldBeValid()
        }
    }

    @Test
    fun `test sequential model`() {
        startKoin {
            modules(listOf(defaultBackendModule()))
        }

        val (model, path) = loadModel("custom_fashion_mnist.h5") {}
        model.shouldBeInstanceOf<Model.Sequential> {
            TrainSequentialModelScriptGenerator(
                TrainState(
                    userOldModelPath = FilePath.Local(path),
                    userNewModelPath = FilePath.Local("/tmp/custom_fashion_mnist-trained.h5"),
                    userDataset = Dataset.ExampleDataset.Mnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 50,
                    userNewModel = it,
                    userValidationSplit = None,
                    generateDebugComments = false,
                    target = ModelDeploymentTarget.Desktop,
                    jobId = Random.nextInt(1, Int.MAX_VALUE)
                ),
                it
            ).generateScript().shouldBeValid()
        }
    }

    @Test
    fun `test loading invalid model`() {
        startKoin {
            modules(defaultBackendModule())
        }

        TrainGeneralModelScriptGenerator(
            TrainState(
                userOldModelPath = FilePath.Local(Paths.get(
                    this::class.java.getResource("badModel1.h5").toURI()
                ).toString()),
                userNewModelPath = FilePath.Local("/tmp/badModel1-trained.h5"),
                userDataset = Dataset.ExampleDataset.Mnist,
                userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                userLoss = Loss.SparseCategoricalCrossentropy,
                userMetrics = setOf("accuracy"),
                userEpochs = 50,
                userNewModel = mockk(),
                userValidationSplit = None,
                generateDebugComments = false,
                target = ModelDeploymentTarget.Desktop,
                jobId = Random.nextInt(1, Int.MAX_VALUE)
            ),
            mockk()
        ).generateScript().shouldBeInvalid()
    }
}
