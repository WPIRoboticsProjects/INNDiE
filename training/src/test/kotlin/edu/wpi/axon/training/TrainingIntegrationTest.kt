package edu.wpi.axon.training

import edu.wpi.axon.dsl.defaultModule
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import java.io.File

internal class TrainingIntegrationTest : KoinTestFixture() {

    @Test
    fun `test with fashion mnist`() {
        startKoin {
            modules(defaultModule())
        }

        val modelName = "custom_fashion_mnist.h5"
        val localModelPath = TrainingIntegrationTest::class.java
            .getResource(modelName).toURI().path
        val layers = LoadLayersFromHDF5().load(File(localModelPath))

        Training(
            userModelPath = modelName,
            userDataset = Dataset.Mnist,
            userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 50,
            userCurrentLayers = layers,
            userNewLayers = layers.map {
                it
            }
        ).generateScript().shouldBeValid {
            println(it.a)
            it.a shouldBe """
            |import tensorflow as tf
            |
            |(xTrain, yTrain), (xTest, yTest) = tf.keras.datasets.mnist.load_data()
            |
            |scaledXTrain = xTrain.reshape(-1, 28, 28, 1) / 255
            |
            |model = tf.keras.models.load_model("custom_fashion_mnist.h5")
            |
            |newModel = tf.keras.Sequential([
            |    model.get_layer("conv2d_6"),
            |    model.get_layer("conv2d_7"),
            |    model.get_layer("max_pooling2d_3"),
            |    model.get_layer("dropout_6"),
            |    model.get_layer("flatten_3"),
            |    model.get_layer("dense_6"),
            |    model.get_layer("dropout_7"),
            |    model.get_layer("dense_7")
            |])
            |newModel.get_layer("conv2d_6").trainable = False
            |newModel.get_layer("conv2d_7").trainable = False
            |newModel.get_layer("max_pooling2d_3").trainable = False
            |newModel.get_layer("dropout_6").trainable = False
            |newModel.get_layer("flatten_3").trainable = False
            |newModel.get_layer("dense_6").trainable = True
            |newModel.get_layer("dropout_7").trainable = True
            |newModel.get_layer("dense_7").trainable = True
            |
            |scaledXTest = xTest.reshape(-1, 28, 28, 1) / 255
            |
            |newModel.compile(
            |    optimizer=tf.keras.optimizers.Adam(0.001, 0.9, 0.999, 1.0E-7, False),
            |    loss=tf.keras.losses.sparse_categorical_crossentropy,
            |    metrics=["accuracy"]
            |)
            |
            |newModel.fit(
            |    scaledXTrain,
            |    yTrain,
            |    batch_size=None,
            |    epochs=50,
            |    verbose=2,
            |    callbacks=[],
            |    validation_data=(scaledXTest, yTest),
            |    shuffle=True
            |)
            """.trimMargin()
        }
    }
}
