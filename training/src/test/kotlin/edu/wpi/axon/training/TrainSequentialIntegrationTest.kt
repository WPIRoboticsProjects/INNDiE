@file:SuppressWarnings("LongMethod", "LargeClass")

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
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class TrainSequentialIntegrationTest : KoinTestFixture() {

    @Test
    fun `test with bad model`() {
        startKoin {
            modules(defaultModule())
        }

        val localModelPath = this::class.java.getResource("badModel1.h5").toURI().path
        TrainSequential(
            userOldModelPath = localModelPath,
            userNewModelPath = "badModel1-trained.h5",
            userBucketName = getTestBucketName(),
            userRegion = getTestRegion(),
            userDataset = Dataset.Mnist,
            userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 50,
            userNewLayers = emptySet()
        ).generateScript().shouldBeInvalid()
    }

    @Test
    fun `test with fashion mnist`() {
        startKoin {
            modules(defaultModule())
        }

        val modelName = "custom_fashion_mnist.h5"
        val newModelName = "custom_fashion_mnist-trained.h5"
        val (model, path) = loadModel(modelName)
        model.shouldBeInstanceOf<Model.Sequential> {
            TrainSequential(
                userOldModelPath = path,
                userNewModelPath = newModelName,
                userBucketName = getTestBucketName(),
                userRegion = getTestRegion(),
                userDataset = Dataset.Mnist,
                userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                userLoss = Loss.SparseCategoricalCrossentropy,
                userMetrics = setOf("accuracy"),
                userEpochs = 50,
                userNewLayers = it.layers.mapIndexedTo(mutableSetOf()) { index, layer ->
                    // Only train the last 3 layers
                    if (it.layers.size - index <= 3) layer.layer.trainable()
                    else layer.layer.trainable(false)
                }
            ).generateScript().shouldBeValid {
                it.a shouldBe """
                |import axon.client
                |import tensorflow as tf
                |
                |axon.client.impl_download_model_file("$modelName", "${getTestBucketName()}", "${getTestRegion()}")
                |
                |model = tf.keras.models.load_model("$modelName")
                |
                |newModel = tf.keras.Sequential([
                |    model.get_layer("conv2d_4"),
                |    model.get_layer("conv2d_5"),
                |    model.get_layer("max_pooling2d_2"),
                |    model.get_layer("dropout_4"),
                |    model.get_layer("flatten_2"),
                |    model.get_layer("dense_4"),
                |    model.get_layer("dropout_5"),
                |    model.get_layer("dense_5")
                |])
                |newModel.get_layer("conv2d_4").trainable = False
                |newModel.get_layer("conv2d_5").trainable = False
                |newModel.get_layer("max_pooling2d_2").trainable = False
                |newModel.get_layer("dropout_4").trainable = False
                |newModel.get_layer("flatten_2").trainable = False
                |newModel.get_layer("dense_4").trainable = True
                |newModel.get_layer("dropout_5").trainable = True
                |newModel.get_layer("dense_5").trainable = True
                |
                |checkpointCallback = tf.keras.callbacks.ModelCheckpoint(
                |    "sequential_2-weights.{epoch:02d}-{val_loss:.2f}.hdf5",
                |    monitor="val_loss",
                |    verbose=1,
                |    save_best_only=False,
                |    save_weights_only=True,
                |    mode="auto",
                |    save_freq="epoch",
                |    load_weights_on_restart=False
                |)
                |
                |newModel.compile(
                |    optimizer=tf.keras.optimizers.Adam(0.001, 0.9, 0.999, 1.0E-7, False),
                |    loss=tf.keras.losses.sparse_categorical_crossentropy,
                |    metrics=["accuracy"]
                |)
                |
                |earlyStoppingCallback = tf.keras.callbacks.EarlyStopping(
                |    monitor="val_loss",
                |    min_delta=0,
                |    patience=10,
                |    verbose=1,
                |    mode="auto",
                |    baseline=None,
                |    restore_best_weights=False
                |)
                |
                |(xTrain, yTrain), (xTest, yTest) = tf.keras.datasets.mnist.load_data()
                |
                |scaledXTest = xTest.reshape(-1, 28, 28, 1) / 255
                |
                |scaledXTrain = xTrain.reshape(-1, 28, 28, 1) / 255
                |
                |newModel.fit(
                |    scaledXTrain,
                |    yTrain,
                |    batch_size=None,
                |    epochs=50,
                |    verbose=2,
                |    callbacks=[checkpointCallback, earlyStoppingCallback],
                |    validation_data=(scaledXTest, yTest),
                |    shuffle=True
                |)
                |
                |newModel.save("$newModelName")
                |
                |axon.client.impl_upload_model_file("$newModelName", "${getTestBucketName()}", "${getTestRegion()}")
                """.trimMargin()
            }
        }
    }
}
