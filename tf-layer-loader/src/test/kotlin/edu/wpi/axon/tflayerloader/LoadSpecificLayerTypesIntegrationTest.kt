@file:SuppressWarnings("LongMethod", "LargeClass")
@file:Suppress("UnstableApiUsage")

package edu.wpi.axon.tflayerloader

import arrow.core.None
import arrow.core.Right
import arrow.core.Tuple2
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.DataFormat
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
                    None,
                    Right(Tuple2(2, 2)),
                    Right(Tuple2(2, 2)),
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
                    None,
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
                    None,
                    0.2,
                    null
                ).trainable()
            )
        }
    }
}
