package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tfdata.layer.trainable
import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class SequentialLayerToCodeTest {

    private val layerToCode = SequentialLayerToCode()

    @ParameterizedTest
    @MethodSource("layerSource")
    fun `test layers`(layer: Layer, expected: Either<String, String>) {
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
                SealedLayer.Dense("name", None, 3, Activation.ReLu),
                """tf.keras.layers.Dense(name="name", units=3, activation=tf.keras.activations.relu)""".right()
            ),
            Arguments.of(
                SealedLayer.Dense("name", Some(setOf("input_name")), 3, Activation.ReLu),
                """Must have a layer with no declared inputs, got Some([input_name])""".left()
            ),
            Arguments.of(
                SealedLayer.Dense("name", None, 3, Activation.ReLu).trainable(),
                """tf.keras.layers.Dense(name="name", units=3, activation=tf.keras.activations.relu)""".right()
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
