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
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class TrainGeneralIntegrationTest : KoinTestFixture() {

    @Test
    fun `test with custom model with an add`() {
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
                    userBucketName = getTestBucketName(),
                    userRegion = getTestRegion(),
                    userDataset = Dataset.Mnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 50,
                    userNewModel = it
                )
            ).generateScript().shouldBeValid {
                it.a shouldBe """
                |import axon.client
                |import tensorflow as tf
                |
                |axon.client.impl_download_model_file("$modelName", "${getTestBucketName()}", "${getTestRegion()}")
                |
                |model = tf.keras.models.load_model("$modelName")
                |
                |var1 = tf.keras.Input(shape=(16,), batch_size=None, dtype=None, sparse=False)
                |var2 = model.get_layer("dense_18")(var1)
                |var3 = tf.keras.Input(shape=(32,), batch_size=None, dtype=None, sparse=False)
                |var4 = model.get_layer("dense_19")(var3)
                |var5 = model.get_layer("add_4")([var2, var4])
                |var6 = model.get_layer("dense_20")(var5)
                |newModelVar = tf.keras.Model(inputs=[var1, var3], outputs=[var6])
                |newModelVar.get_layer("dense_18").trainable = True
                |newModelVar.get_layer("dense_19").trainable = True
                |newModelVar.get_layer("add_4").trainable = True
                |newModelVar.get_layer("dense_20").trainable = True
                |
                |checkpointCallback = tf.keras.callbacks.ModelCheckpoint(
                |    "model_6-weights.{epoch:02d}-{val_loss:.2f}.hdf5",
                |    monitor="val_loss",
                |    verbose=1,
                |    save_best_only=False,
                |    save_weights_only=True,
                |    mode="auto",
                |    save_freq="epoch",
                |    load_weights_on_restart=False
                |)
                |
                |newModelVar.compile(
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
                |newModelVar.fit(
                |    xTrain,
                |    yTrain,
                |    batch_size=None,
                |    epochs=50,
                |    verbose=2,
                |    callbacks=[checkpointCallback, earlyStoppingCallback],
                |    validation_data=(xTest, yTest),
                |    shuffle=True
                |)
                |
                |newModelVar.save("$newModelName")
                |
                |axon.client.impl_upload_model_file("$newModelName", "${getTestBucketName()}", "${getTestRegion()}")
                """.trimMargin()
            }
        }
    }
}
