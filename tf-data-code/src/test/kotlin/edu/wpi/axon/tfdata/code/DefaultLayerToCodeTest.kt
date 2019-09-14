package edu.wpi.axon.tfdata.code

import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tfdata.layer.trainable
import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DefaultLayerToCodeTest {

    private val layerToCode = DefaultLayerToCode()

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
                SealedLayer.Dense("name", 3, Activation.ReLu),
                """tf.keras.layers.Dense(name="name", units=3, activation=tf.keras.activations.relu)"""
            ),
            Arguments.of(
                SealedLayer.Dense("name", 3, Activation.ReLu).trainable(),
                """tf.keras.layers.Dense(name="name", units=3, activation=tf.keras.activations.relu)"""
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
