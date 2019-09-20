package edu.wpi.axon.tflayerloader

import arrow.core.Tuple2
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tfdata.layer.trainable
import io.kotlintest.assertions.arrow.either.shouldBeLeft
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

internal class LoadLayersFromHDF5Test {

    @Test
    fun `load from test file 1`() {
        LoadLayersFromHDF5().load(
            File(LoadLayersFromHDF5Test::class.java.getResource("model1.h5").toURI())
        ).attempt().unsafeRunSync().shouldBeRight { model ->
            model.shouldBeInstanceOf<Model.Sequential> {
                it.name shouldBe "sequential_11"
                it.batchInputShape.shouldContainExactly(null, 28, 28, 1)
                it.layers.shouldContainExactly(
                    setOf(
                        SealedLayer.Conv2D(
                            "conv2d_16",
                            32,
                            Tuple2(3, 3),
                            Activation.ReLu
                        ).trainable(),
                        SealedLayer.Conv2D(
                            "conv2d_17",
                            64,
                            Tuple2(3, 3),
                            Activation.ReLu
                        ).trainable(),
                        SealedLayer.UnknownLayer("max_pooling2d_8").trainable(),
                        SealedLayer.UnknownLayer("dropout_19").trainable(),
                        SealedLayer.UnknownLayer("flatten_8").trainable(),
                        SealedLayer.Dense("dense_22", 128, Activation.ReLu).trainable(),
                        SealedLayer.UnknownLayer("dropout_20").trainable(),
                        SealedLayer.Dense("dense_23", 10, Activation.SoftMax).trainable()
                    )
                )
            }
        }
    }

    @Test
    fun `load from test file 2`() {
        LoadLayersFromHDF5().load(
            File(LoadLayersFromHDF5Test::class.java.getResource("model2.h5").toURI())
        ).attempt().unsafeRunSync().shouldBeRight { model ->
            model.shouldBeInstanceOf<Model.Sequential> {
                it.name shouldBe "mobilenetv2_1.00_224"
                it.batchInputShape.shouldContainExactly(null, 224, 224, 3)
                it.layers.shouldHaveSize(157)
            }
        }
    }

    @Test
    fun `load from bad file`() {
        LoadLayersFromHDF5().load(
            File(LoadLayersFromHDF5Test::class.java.getResource("badModel1.h5").toURI())
        ).attempt().unsafeRunSync().shouldBeLeft()
    }
}
