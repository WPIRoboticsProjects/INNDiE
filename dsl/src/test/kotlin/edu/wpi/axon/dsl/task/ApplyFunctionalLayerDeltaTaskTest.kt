package edu.wpi.axon.dsl.task

import arrow.core.None
import arrow.core.Some
import arrow.core.right
import edu.wpi.axon.dsl.alwaysValidImportValidator
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.dsl.mockVariableNameGenerator
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.code.layer.LayerToCode
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tfdata.layer.trainable
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class ApplyFunctionalLayerDeltaTaskTest : KoinTestFixture() {

    @Test
    fun `a current layer with no inputs fails`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = setOf(
                SealedLayer.InputLayer("", listOf()),
                SealedLayer.UnknownLayer("", None).trainable()
            )
            newLayers = setOf(
                SealedLayer.InputLayer("", listOf()),
                SealedLayer.UnknownLayer("", Some(setOf())).trainable()
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeFalse()
    }

    @Test
    fun `a new layer with no inputs fails`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = setOf(
                SealedLayer.InputLayer("", listOf()),
                SealedLayer.UnknownLayer("", Some(setOf())).trainable()
            )
            newLayers = setOf(
                SealedLayer.InputLayer("", listOf()),
                SealedLayer.UnknownLayer("", None).trainable()
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeFalse()
    }

    @Test
    fun `current and new layers both with inputs are fine`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = setOf(
                SealedLayer.InputLayer("", listOf()),
                SealedLayer.UnknownLayer("", Some(setOf())).trainable()
            )
            newLayers = setOf(
                SealedLayer.InputLayer("", listOf()),
                SealedLayer.UnknownLayer("", Some(setOf())).trainable()
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
    }

    @Test
    fun `a layer with a missing input fails`() {
        val layer1 = SealedLayer.InputLayer("l1", listOf())
        val layer2 = SealedLayer.UnknownLayer("l2", Some(setOf(layer1.name)))
        val layer3 = SealedLayer.UnknownLayer("l3", Some(setOf(layer2.name)))

        startKoin {
            modules(module {
                alwaysValidImportValidator()
                mockVariableNameGenerator()
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = setOf(layer1, layer2.trainable(), layer3.trainable())
            newLayers = setOf(layer1, layer3.trainable())
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeFalse()
    }

    @Test
    fun `keep all 2 layers`() {
        val layer1 = SealedLayer.InputLayer("l1", listOf())
        val layer2 = SealedLayer.UnknownLayer("l2", Some(setOf(layer1.name)))

        startKoin {
            modules(module {
                alwaysValidImportValidator()
                mockVariableNameGenerator()
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = setOf(layer1, layer2.trainable())
            newLayers = setOf(layer1, layer2.trainable())
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = base_model.input
            |var2 = base_model.get_layer("l2")(var1)
            |new_model = tf.keras.Model(inputs=var1, outputs=var2)
            |new_model.get_layer("l2").trainable = True
        """.trimMargin()
    }

    @Test
    fun `remove one layer`() {
        val layer1 = SealedLayer.InputLayer("l1", listOf())
        val layer2 = SealedLayer.UnknownLayer("l2", Some(setOf(layer1.name)))
        val layer3 = SealedLayer.UnknownLayer("l3", Some(setOf(layer2.name)))

        startKoin {
            modules(module {
                alwaysValidImportValidator()
                mockVariableNameGenerator()
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = setOf(layer1, layer2.trainable(), layer3.trainable())
            newLayers = setOf(layer1, layer2.trainable())
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = base_model.input
            |var2 = base_model.get_layer("l2")(var1)
            |new_model = tf.keras.Model(inputs=var1, outputs=var2)
            |new_model.get_layer("l2").trainable = True
        """.trimMargin()
    }

    @Test
    fun `replace one layer`() {
        val layer1 = SealedLayer.InputLayer("l1", listOf())
        val layer2 = SealedLayer.UnknownLayer("l2", Some(setOf(layer1.name)))
        val layer3 = SealedLayer.Dense("l3", Some(setOf(layer1.name)), 10, Activation.SoftMax)

        val mockLayerToCode = mockk<LayerToCode> {
            every { makeNewLayer(layer3) } returns "layer3".right()
        }

        startKoin {
            modules(module {
                alwaysValidImportValidator()
                mockVariableNameGenerator()
                single { mockLayerToCode }
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = setOf(layer1, layer2.trainable())
            newLayers = setOf(layer1, layer3.trainable())
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = base_model.input
            |var2 = layer3(var1)
            |new_model = tf.keras.Model(inputs=var1, outputs=var2)
            |new_model.get_layer("l3").trainable = True
        """.trimMargin()

        verifyAll { mockLayerToCode.makeNewLayer(layer3) }
        confirmVerified(mockLayerToCode)
    }
}
