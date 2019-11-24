@file:SuppressWarnings("LongMethod", "LargeClass")
@file:Suppress("UnstableApiUsage")

package edu.wpi.axon.tflayerloader

import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.SerializableEitherITii
import edu.wpi.axon.tfdata.SerializableTuple2II
import edu.wpi.axon.tfdata.layer.DataFormat
import edu.wpi.axon.tfdata.layer.Interpolation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.layer.PoolingPadding
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class LoadSpecificLayerTypesIntegrationTest {

    @Test
    fun `load AvgPool2D`() {
        loadModel<Model.Sequential>("sequential_with_avgpool2d.h5") {
            it.name shouldBe "sequential_7"
            it.batchInputShape shouldBe listOf(null, null, 2, 2)
            it.layers.shouldContainExactly(
                Layer.AveragePooling2D(
                    "average_pooling2d_7",
                    null,
                    SerializableEitherITii.Right(SerializableTuple2II(2, 2)),
                    SerializableEitherITii.Right(SerializableTuple2II(2, 2)),
                    PoolingPadding.Valid,
                    DataFormat.ChannelsLast
                ).trainable()
            )
        }
    }

    @Test
    fun `load GlobalMaxPooling2D`() {
        loadModel<Model.Sequential>("sequential_with_globalmaxpooling2d.h5") {
            it.name shouldBe "sequential_8"
            it.batchInputShape shouldBe listOf(null, null, 2, 2)
            it.layers.shouldContainExactly(
                Layer.GlobalMaxPooling2D(
                    "global_max_pooling2d",
                    null,
                    DataFormat.ChannelsLast
                ).trainable()
            )
        }
    }

    @Test
    fun `load SpatialDropout2D`() {
        loadModel<Model.Sequential>("sequential_with_spatialdropout2d.h5") {
            it.name shouldBe "sequential_9"
            it.batchInputShape shouldBe listOf(null, null, 2, 2)
            it.layers.shouldContainExactly(
                Layer.SpatialDropout2D(
                    "spatial_dropout2d",
                    null,
                    0.2,
                    null
                ).trainable()
            )
        }
    }

    @Test
    fun `load UpSampling2D`() {
        loadModel<Model.Sequential>("sequential_with_upsampling2d_nearest.h5") {
            it.name shouldBe "sequential_3"
            it.batchInputShape shouldBe listOf(null, null, 2, 2)
            it.layers.shouldContainExactly(
                Layer.UpSampling2D(
                    "up_sampling2d_3",
                    null,
                    SerializableEitherITii.Right(SerializableTuple2II(2, 2)),
                    DataFormat.ChannelsLast,
                    Interpolation.Nearest
                ).trainable()
            )
        }
    }

    @Test
    fun `load UpSampling2D bilinear`() {
        loadModel<Model.Sequential>("sequential_with_upsampling2d_bilinear.h5") {
            it.name shouldBe "sequential_2"
            it.batchInputShape shouldBe listOf(null, null, 2, 2)
            it.layers.shouldContainExactly(
                Layer.UpSampling2D(
                    "up_sampling2d_2",
                    null,
                    SerializableEitherITii.Right(SerializableTuple2II(2, 2)),
                    DataFormat.ChannelsLast,
                    Interpolation.Bilinear
                ).trainable()
            )
        }
    }
}
