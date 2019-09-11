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
    fun `no layers to add nor remove`() {
        startKoin {
            modules(module {
                defaultUniqueVariableNameGenerator()
            })
        }

        val task = ApplyLayerDeltaTask("task1").apply {
            modelInput = configuredCorrectly("base_model")
            layersToAdd = listOf()
            layersToRemove = listOf()
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential()
            |var1 = []
            |var2 = []
            |
            |for layer in base_model.layers:
            |   if layer.name not in var2:
            |       new_model.add(layer)
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
            layersToAdd = listOf()
            layersToRemove = listOf(Layer.Dense("dense_1", true, 10, Activation.ReLu))
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential()
            |var1 = []
            |var2 = ["dense_1"]
            |
            |for layer in base_model.layers:
            |   if layer.name not in var2:
            |       new_model.add(layer)
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
            layersToAdd = listOf()
            layersToRemove = listOf(
                Layer.Dense("dense_1", true, 10, Activation.ReLu),
                Layer.UnknownLayer("unknown_1", true)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential()
            |var1 = []
            |var2 = ["dense_1", "unknown_1"]
            |
            |for layer in base_model.layers:
            |   if layer.name not in var2:
            |       new_model.add(layer)
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
            layersToAdd = listOf(Layer.Dense("dense_1", true, 10, Activation.ReLu))
            layersToRemove = listOf()
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential()
            |var1 = [tf.keras.layers.Dense(name="dense_1", trainable=True, units=10, activation=tf.keras.activations.relu)]
            |var2 = []
            |
            |for layer in base_model.layers:
            |   if layer.name not in var2:
            |       new_model.add(layer)
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
            layersToAdd = listOf(
                Layer.Dense("dense_1", true, 128, Activation.ReLu),
                Layer.Dense("dense_2", true, 10, Activation.SoftMax)
            )
            layersToRemove = listOf()
            newModelOutput = configuredCorrectly("new_model")
        }

        task.code() shouldBe """
            |new_model = tf.keras.Sequential()
            |var1 = [tf.keras.layers.Dense(name="dense_1", trainable=True, units=128, activation=tf.keras.activations.relu),
            |        tf.keras.layers.Dense(name="dense_2", trainable=True, units=10, activation=tf.keras.activations.softmax)]
            |var2 = []
            |
            |for layer in base_model.layers:
            |   if layer.name not in var2:
            |       new_model.add(layer)
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
            layersToAdd = listOf(Layer.UnknownLayer("layer_1", true))
            layersToRemove = listOf()
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
            layersToAdd = listOf(
                Layer.Dense("dense_1", true, 128, Activation.UnknownActivation("activation_1"))
            )
            layersToRemove = listOf()
            newModelOutput = configuredCorrectly("new_model")
        }

        shouldThrow<IllegalArgumentException> { task.code() }
    }
}
