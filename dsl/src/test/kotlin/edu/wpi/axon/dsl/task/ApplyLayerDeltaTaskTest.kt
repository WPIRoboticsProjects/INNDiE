@file:SuppressWarnings("TooManyFunctions", "StringLiteralDuplication", "LargeClass")
package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.dsl.defaultUniqueVariableNameGenerator
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.code.LayerToCode
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tfdata.layer.trainable
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class ApplyLayerDeltaTaskTest : KoinTestFixture() {

    @Test
    fun `keep all 1 layers`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val layer1 = SealedLayer.Dense("dense_1", 10, Activation.ReLu).trainable()

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf(layer1)
            newLayers = listOf(layer1)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([base_model.get_layer("dense_1")])
            |new_model.get_layer("dense_1").trainable = True
        """.trimMargin()
    }

    @Test
    fun `keep all 2 layers`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val layer1 = SealedLayer.Dense("dense_1", 10, Activation.ReLu).trainable()
        val layer2 = SealedLayer.UnknownLayer("unknown_1").trainable()

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf(layer1, layer2)
            newLayers = listOf(layer1, layer2)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([
            |    base_model.get_layer("dense_1"),
            |    base_model.get_layer("unknown_1")
            |])
            |new_model.get_layer("dense_1").trainable = True
            |new_model.get_layer("unknown_1").trainable = True
        """.trimMargin()
    }

    @Test
    fun `remove one layer`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf(SealedLayer.Dense("dense_1", 10, Activation.ReLu).trainable())
            newLayers = listOf()
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([])
            |
        """.trimMargin()
    }

    @Test
    fun `remove two layers`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf(
                SealedLayer.Dense("dense_1", 10, Activation.ReLu).trainable(),
                SealedLayer.UnknownLayer("unknown_1").trainable()
            )
            newLayers = listOf()
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([])
            |
        """.trimMargin()
    }

    @Test
    fun `add one layer`() {
        val layer1 = SealedLayer.Dense("dense_1", 10, Activation.ReLu).trainable()

        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
                single<LayerToCode> {
                    mockk {
                        every { makeNewLayer(layer1) } returns "layer1"
                    }
                }
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf()
            newLayers = listOf(layer1)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([layer1])
            |new_model.get_layer("dense_1").trainable = True
        """.trimMargin()
    }

    @Test
    fun `add two layers`() {
        val layer1 = SealedLayer.Dense("dense_1", 128, Activation.ReLu).trainable()
        val layer2 = SealedLayer.Dense("dense_2", 10, Activation.SoftMax).trainable()

        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
                single<LayerToCode> {
                    mockk {
                        every { makeNewLayer(layer1) } returns "layer1"
                        every { makeNewLayer(layer2) } returns "layer2"
                    }
                }
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf()
            newLayers = listOf(layer1, layer2)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([
            |    layer1,
            |    layer2
            |])
            |new_model.get_layer("dense_1").trainable = True
            |new_model.get_layer("dense_2").trainable = True
        """.trimMargin()
    }

    @Test
    fun `remove the first layer and replace the second and swap them`() {
        val layer1 = SealedLayer.UnknownLayer("unknown_3").trainable()
        val layer2Old = SealedLayer.Dense("dense_2", 10, Activation.SoftMax).trainable()
        val layer2New = SealedLayer.Dense("dense_2", 3, Activation.SoftMax).trainable()

        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
                single<LayerToCode> {
                    mockk {
                        every { makeNewLayer(layer2New) } returns "layer2New"
                    }
                }
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf(layer1, layer2Old)
            newLayers = listOf(layer2New, layer1)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([
            |    layer2New,
            |    base_model.get_layer("unknown_3")
            |])
            |new_model.get_layer("dense_2").trainable = True
            |new_model.get_layer("unknown_3").trainable = True
        """.trimMargin()
    }

    @Test
    fun `copy an unknown layer`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val layer1 = SealedLayer.UnknownLayer("unknown_1").trainable()

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf(layer1)
            newLayers = listOf(layer1)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([base_model.get_layer("unknown_1")])
            |new_model.get_layer("unknown_1").trainable = True
        """.trimMargin()
    }

    @Test
    fun `copy a layer with an unknown activation function`() {
        val layer1 = SealedLayer.Dense("dense_1", 10, Activation.UnknownActivation("activation_1"))
            .trainable()

        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf(layer1)
            newLayers = listOf(layer1)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([base_model.get_layer("dense_1")])
            |new_model.get_layer("dense_1").trainable = True
        """.trimMargin()
    }
}
