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
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class TrainIntegrationTest : KoinTestFixture() {

    @Test
    fun `test general model`() {
        startKoin {
            modules(defaultModule())
        }

        val (model, path) = loadModel("network_with_add.h5")
        model.shouldBeInstanceOf<Model.General> {
            Train(
                userModelPath = path,
                userBucketName = userBucketName,
                userRegion = userRegion,
                userDataset = Dataset.Mnist,
                userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                userLoss = Loss.SparseCategoricalCrossentropy,
                userMetrics = setOf("accuracy"),
                userEpochs = 50,
                userNewModel = it
            ).generateScript().shouldBeValid()
        }
    }

    @Test
    fun `test sequential model`() {
        startKoin {
            modules(defaultModule())
        }

        val (model, path) = loadModel("custom_fashion_mnist.h5")
        model.shouldBeInstanceOf<Model.Sequential> {
            Train(
                userModelPath = path,
                userBucketName = userBucketName,
                userRegion = userRegion,
                userDataset = Dataset.Mnist,
                userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                userLoss = Loss.SparseCategoricalCrossentropy,
                userMetrics = setOf("accuracy"),
                userEpochs = 50,
                userNewModel = model
            ).generateScript().shouldBeValid()
        }
    }

    @Test
    fun `test sequential model on disk with general new model`() {
        startKoin {
            modules(defaultModule())
        }

        val (sequentialModel, sequentialPath) = loadModel("custom_fashion_mnist.h5")
        val (generalModel, _) = loadModel("network_with_add.h5")
        sequentialModel.shouldBeInstanceOf<Model.Sequential> {
            Train(
                userModelPath = sequentialPath,
                userBucketName = userBucketName,
                userRegion = userRegion,
                userDataset = Dataset.Mnist,
                userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                userLoss = Loss.SparseCategoricalCrossentropy,
                userMetrics = setOf("accuracy"),
                userEpochs = 50,
                userNewModel = generalModel
            ).generateScript().shouldBeInvalid()
        }
    }

    @Test
    fun `test general model on disk with sequential new model`() {
        startKoin {
            modules(defaultModule())
        }

        val (generalModel, generalPath) = loadModel("network_with_add.h5")
        val (sequentialModel, _) = loadModel("custom_fashion_mnist.h5")
        generalModel.shouldBeInstanceOf<Model.General> {
            Train(
                userModelPath = generalPath,
                userBucketName = userBucketName,
                userRegion = userRegion,
                userDataset = Dataset.Mnist,
                userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                userLoss = Loss.SparseCategoricalCrossentropy,
                userMetrics = setOf("accuracy"),
                userEpochs = 50,
                userNewModel = sequentialModel
            ).generateScript().shouldBeInvalid()
        }
    }

    @Test
    fun `test loading invalid model`() {
        startKoin {
            modules(defaultModule())
        }

        Train(
            userModelPath = this::class.java.getResource("badModel1.h5").toURI().path,
            userBucketName = userBucketName,
            userRegion = userRegion,
            userDataset = Dataset.Mnist,
            userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 50,
            userNewModel = mockk()
        ).generateScript().shouldBeInvalid()
    }
}
