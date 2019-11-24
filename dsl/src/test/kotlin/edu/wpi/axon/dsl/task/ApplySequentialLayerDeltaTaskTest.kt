@file:SuppressWarnings("TooManyFunctions", "StringLiteralDuplication", "LargeClass")

package edu.wpi.axon.dsl.task

import arrow.core.right
import edu.wpi.axon.dsl.alwaysValidImportValidator
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.dsl.mockVariableNameGenerator
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.code.layer.LayerToCode
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import io.kotlintest.matchers.booleans.shouldBeFalse
import io.kotlintest.matchers.booleans.shouldBeTrue
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class ApplySequentialLayerDeltaTaskTest : KoinTestFixture() {

    @Test
    fun `keep all 1 layers`() {
        startKoin {}

        val layer1 = Layer.Dense("dense_1", null, 10, Activation.ReLu).trainable()

        val task = ApplySequentialLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            oldLayers = setOf(layer1)
            newLayers = setOf(layer1)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([base_model.get_layer("dense_1")])
            |new_model.get_layer("dense_1").trainable = True
        """.trimMargin()
    }

    @Test
    fun `keep all 2 layers`() {
        startKoin {}

        val layer1 = Layer.Dense("dense_1", null, 10, Activation.ReLu).trainable()
        val layer2 = Layer.UnknownLayer("unknown_1", null).trainable()

        val task = ApplySequentialLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            oldLayers = setOf(layer1, layer2)
            newLayers = setOf(layer1, layer2)
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
        startKoin {}

        val task = ApplySequentialLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            oldLayers =
                setOf(Layer.Dense("dense_1", null, 10, Activation.ReLu).trainable())
            newLayers = setOf()
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([])
            |
        """.trimMargin()
    }

    @Test
    fun `remove two layers`() {
        startKoin {}

        val task = ApplySequentialLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            oldLayers = setOf(
                Layer.Dense("dense_1", null, 10, Activation.ReLu).trainable(),
                Layer.UnknownLayer("unknown_1", null).trainable()
            )
            newLayers = setOf()
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([])
            |
        """.trimMargin()
    }

    @Test
    fun `add one layer`() {
        val layer1 = Layer.Dense("dense_1", null, 10, Activation.ReLu).trainable()

        startKoin {
            modules(module {
                mockVariableNameGenerator()
                single<LayerToCode> {
                    mockk {
                        every { makeNewLayer(layer1) } returns "layer1".right()
                    }
                }
            })
        }

        val task = ApplySequentialLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            oldLayers = setOf()
            newLayers = setOf(layer1)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([layer1])
            |new_model.get_layer("dense_1").trainable = True
        """.trimMargin()
    }

    @Test
    fun `add two layers`() {
        val layer1 = Layer.Dense("dense_1", null, 128, Activation.ReLu).trainable()
        val layer2 = Layer.Dense("dense_2", null, 10, Activation.SoftMax).trainable()

        startKoin {
            modules(module {
                mockVariableNameGenerator()
                single<LayerToCode> {
                    mockk {
                        every { makeNewLayer(layer1) } returns "layer1".right()
                        every { makeNewLayer(layer2) } returns "layer2".right()
                    }
                }
            })
        }

        val task = ApplySequentialLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            oldLayers = setOf()
            newLayers = setOf(layer1, layer2)
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
        val layer1 = Layer.UnknownLayer("unknown_3", null).trainable()
        val layer2Old = Layer.Dense("dense_2", null, 10, Activation.SoftMax).trainable()
        val layer2New = Layer.Dense("dense_2", null, 3, Activation.SoftMax).trainable()

        startKoin {
            modules(module {
                mockVariableNameGenerator()
                single<LayerToCode> {
                    mockk {
                        every { makeNewLayer(layer2New) } returns "layer2New".right()
                    }
                }
            })
        }

        val task = ApplySequentialLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            oldLayers = setOf(layer1, layer2Old)
            newLayers = setOf(layer2New, layer1)
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
        startKoin {}

        val layer1 = Layer.UnknownLayer("unknown_1", null).trainable()

        val task = ApplySequentialLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            oldLayers = setOf(layer1)
            newLayers = setOf(layer1)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([base_model.get_layer("unknown_1")])
            |new_model.get_layer("unknown_1").trainable = True
        """.trimMargin()
    }

    @Test
    fun `copy a layer with an unknown activation function`() {
        val layer1 =
            Layer.Dense("dense_1", null, 10, Activation.UnknownActivation("activation_1"))
                .trainable()
        startKoin {}

        val task = ApplySequentialLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            oldLayers = setOf(layer1)
            newLayers = setOf(layer1)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([base_model.get_layer("dense_1")])
            |new_model.get_layer("dense_1").trainable = True
        """.trimMargin()
    }

    @Test
    fun `a current layer with inputs fails`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = ApplySequentialLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            oldLayers = setOf(Layer.UnknownLayer("", setOf()).trainable())
            newLayers = setOf(Layer.UnknownLayer("", null).trainable())
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeFalse()
    }

    @Test
    fun `a new layer with inputs fails`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = ApplySequentialLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            oldLayers = setOf(Layer.UnknownLayer("", null).trainable())
            newLayers = setOf(Layer.UnknownLayer("", setOf()).trainable())
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeFalse()
    }

    @Test
    fun `current and new layers without any inputs are fine`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = ApplySequentialLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            oldLayers = setOf(Layer.UnknownLayer("", null).trainable())
            newLayers = setOf(Layer.UnknownLayer("", null).trainable())
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
    }
}
