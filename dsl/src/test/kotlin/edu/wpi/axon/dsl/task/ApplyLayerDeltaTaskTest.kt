package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.dsl.defaultUniqueVariableNameGenerator
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tflayers.Activation
import edu.wpi.axon.tflayers.Layer
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
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

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf(Layer.Dense("dense_1", true, 10, Activation.ReLu))
            newLayers = listOf(Layer.Dense("dense_1", true, 10, Activation.ReLu))
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([base_model.get_layer("dense_1")])
        """.trimMargin()
    }

    @Test
    fun `keep all 2 layers`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf(
                Layer.Dense("dense_1", true, 10, Activation.ReLu),
                Layer.UnknownLayer("unknown_1", true)
            )
            newLayers = listOf(
                Layer.Dense("dense_1", true, 10, Activation.ReLu),
                Layer.UnknownLayer("unknown_1", true)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([
            |    base_model.get_layer("dense_1"),
            |    base_model.get_layer("unknown_1")
            |])
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
            currentLayers = listOf(Layer.Dense("dense_1", true, 10, Activation.ReLu))
            newLayers = listOf()
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([])
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
                Layer.Dense("dense_1", true, 10, Activation.ReLu),
                Layer.UnknownLayer("unknown_1", true)
            )
            newLayers = listOf()
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([])
        """.trimMargin()
    }

    @Test
    fun `add one layer`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf()
            newLayers = listOf(Layer.Dense("dense_1", true, 10, Activation.ReLu))
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([tf.keras.layers.Dense(name="dense_1", trainable=True, units=10, activation=tf.keras.activations.relu)])
        """.trimMargin()
    }

    @Test
    fun `add two layers`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf()
            newLayers = listOf(
                Layer.Dense("dense_1", true, 128, Activation.ReLu),
                Layer.Dense("dense_2", true, 10, Activation.SoftMax)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([
            |    tf.keras.layers.Dense(name="dense_1", trainable=True, units=128, activation=tf.keras.activations.relu),
            |    tf.keras.layers.Dense(name="dense_2", trainable=True, units=10, activation=tf.keras.activations.softmax)
            |])
        """.trimMargin()
    }

    @Test
    fun `add an unknown layer`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf()
            newLayers = listOf(Layer.UnknownLayer("layer_1", true))
            newModelOutput = configuredCorrectly("new_model")
        }

        shouldThrow<IllegalArgumentException> { task.code() }
    }

    @Test
    fun `add layer with an unknown activation function`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf()
            newLayers = listOf(
                Layer.Dense("dense_1", true, 128, Activation.UnknownActivation("activation_1"))
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        shouldThrow<IllegalArgumentException> { task.code() }
    }

    @Test
    fun `remove the first layer and replace the second`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf(
                Layer.UnknownLayer("unknown_3", true),
                Layer.Dense("dense_2", true, 10, Activation.SoftMax)
            )
            newLayers = listOf(
                Layer.UnknownLayer("unknown_3", true),
                Layer.Dense("dense_2", true, 3, Activation.SoftMax)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([
            |    base_model.get_layer("unknown_3"),
            |    tf.keras.layers.Dense(name="dense_2", trainable=True, units=3, activation=tf.keras.activations.softmax)
            |])
        """.trimMargin()
    }

    @Test
    fun `copy an unknown layer`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf(Layer.UnknownLayer("unknown_1", true))
            newLayers = listOf(Layer.UnknownLayer("unknown_1", true))
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([base_model.get_layer("unknown_1")])
        """.trimMargin()
    }

    @Test
    fun `copy a layer with an unknown activation function`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            currentLayers = listOf(
                Layer.Dense("dense_1", true, 10, Activation.UnknownActivation("activation_1"))
            )
            newLayers = listOf(
                Layer.Dense("dense_1", true, 10, Activation.UnknownActivation("activation_1"))
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential([base_model.get_layer("dense_1")])
        """.trimMargin()
    }
}
