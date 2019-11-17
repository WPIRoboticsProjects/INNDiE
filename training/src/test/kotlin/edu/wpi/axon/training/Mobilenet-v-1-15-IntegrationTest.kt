@file:SuppressWarnings("LongMethod", "LargeClass")

package edu.wpi.axon.training

import arrow.core.None
import edu.wpi.axon.dsl.defaultModule
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.DataFormat
import edu.wpi.axon.tfdata.layer.Layer
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class `Mobilenet-v-1-15-IntegrationTest` : KoinTestFixture() {

    @Test
    fun `test with mobilenet`() {
        startKoin {
            modules(defaultModule())
        }

        val modelName = "mobilenet_tf_1_15_0.h5"
        val (model, _) = loadModel(modelName)
        model.shouldBeInstanceOf<Model.Sequential> {
            it.layers.shouldHaveSize(3)
            it.layers.toList().let {
                it[0].shouldBeInstanceOf<Layer.MetaLayer.UntrainableLayer> {
                    it.layer.shouldBeInstanceOf<Layer.ModelLayer> {
                        it.model.shouldBeInstanceOf<Model.General> {
                            it.layers.nodes().shouldHaveSize(155)
                        }
                    }
                }

                it[1] shouldBe Layer.GlobalAveragePooling2D(
                    "global_average_pooling2d",
                    None,
                    DataFormat.ChannelsLast
                ).trainable()

                it[2] shouldBe Layer.Dense(
                    "dense",
                    None,
                    10,
                    activation = Activation.SoftMax
                ).trainable()
            }
        }
    }
}
