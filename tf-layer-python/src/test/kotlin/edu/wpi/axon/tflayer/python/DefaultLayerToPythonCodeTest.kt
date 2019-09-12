package edu.wpi.axon.tflayer.python

import edu.wpi.axon.tflayers.Activation
import edu.wpi.axon.tflayers.Layer
import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DefaultLayerToPythonCodeTest {

    private val layerToCode = DefaultLayerToPythonCode()

    @ParameterizedTest
    @MethodSource("layerSource")
    fun `test layers`(layer: Layer, expected: String) {
        layerToCode.makeNewLayer(layer) shouldBe expected
    }

    @ParameterizedTest
    @MethodSource("activationSource")
    fun `test activations`(activation: Activation, expected: String) {
        layerToCode.makeNewActivation(activation) shouldBe expected
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun layerSource() = listOf(
            Arguments.of(
                Layer.Dense("name", true, 3, Activation.ReLu),
                """tf.keras.layers.Dense(name="name", trainable=True, units=3, activation=tf.keras.activations.relu)"""
            )
        )

        @JvmStatic
        @Suppress("unused")
        fun activationSource() = listOf(
            Arguments.of(Activation.ReLu, "tf.keras.activations.relu"),
            Arguments.of(Activation.SoftMax, "tf.keras.activations.softmax")
        )
    }
}
