package edu.wpi.axon.aws

import edu.wpi.axon.testutil.KoinTestFixture
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.model.InstanceType

internal class EC2TrainingScriptRunnerTest : KoinTestFixture() {

    @Test
    @Disabled("Needs EC2 supervision.")
    fun `test running mnist training script`() {
        startKoin {
            modules(module {
                single { Region.US_EAST_1 }
                single(named("bucketName")) { "axon-salmon-testbucket2" }
                single { InstanceType.T3_MEDIUM }
            })
        }

        val runner = EC2TrainingScriptRunner()
        runner.startScript(
            "custom_fashion_mnist.h5",
            "custom_fashion_mnist-trained.h5",
            """
            import tensorflow as tf

            var10 = tf.keras.models.load_model("custom_fashion_mnist.h5")

            var12 = tf.keras.Sequential([
                var10.get_layer("conv2d_4"),
                var10.get_layer("conv2d_5"),
                var10.get_layer("max_pooling2d_2"),
                var10.get_layer("dropout_4"),
                var10.get_layer("flatten_2"),
                var10.get_layer("dense_4"),
                var10.get_layer("dropout_5"),
                var10.get_layer("dense_5")
            ])
            var12.get_layer("conv2d_4").trainable = False
            var12.get_layer("conv2d_5").trainable = False
            var12.get_layer("max_pooling2d_2").trainable = False
            var12.get_layer("dropout_4").trainable = False
            var12.get_layer("flatten_2").trainable = False
            var12.get_layer("dense_4").trainable = True
            var12.get_layer("dropout_5").trainable = True
            var12.get_layer("dense_5").trainable = True

            var12.compile(
                optimizer=tf.keras.optimizers.Adam(0.001, 0.9, 0.999, 1.0E-7, False),
                loss=tf.keras.losses.sparse_categorical_crossentropy,
                metrics=["accuracy"]
            )

            var15 = tf.keras.callbacks.ModelCheckpoint(
                "sequential_2-weights.{epoch:02d}-{val_loss:.2f}.hdf5",
                monitor="val_loss",
                verbose=1,
                save_best_only=False,
                save_weights_only=True,
                mode="auto",
                save_freq="epoch",
                load_weights_on_restart=False
            )

            var17 = tf.keras.callbacks.EarlyStopping(
                monitor="val_loss",
                min_delta=0,
                patience=10,
                verbose=1,
                mode="auto",
                baseline=None,
                restore_best_weights=False
            )

            (var1, var2), (var3, var4) = tf.keras.datasets.mnist.load_data()

            var6 = var1.reshape(-1, 28, 28, 1) / 255

            var8 = var3.reshape(-1, 28, 28, 1) / 255

            var12.fit(
                var6,
                var2,
                batch_size=None,
                epochs=1,
                verbose=2,
                callbacks=[var15, var17],
                validation_data=(var8, var4),
                shuffle=True
            )

            var12.save("custom_fashion_mnist-trained.h5")
            """.trimIndent()
        ).unsafeRunSync()
    }
}
