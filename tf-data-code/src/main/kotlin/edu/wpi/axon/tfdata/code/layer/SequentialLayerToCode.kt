package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.layer.SealedLayer

/**
 * Only works for layers in Sequential models (i.e., layers that do not have any [Layer.inputs]).
 */
class SequentialLayerToCode : LayerToCode {

    override fun makeNewLayer(layer: Layer): Either<String, String> = when (layer.inputs) {
        is None -> {
            when (layer) {
                is SealedLayer.MetaLayer -> makeNewLayer(layer.layer)

                is SealedLayer.Dense -> ("""tf.keras.layers.Dense(name="${layer.name}", """ +
                    "units=${layer.units}, " +
                    "activation=${makeNewActivation(layer.activation)})").right()

                else -> "Cannot construct an unknown layer: $layer".left()
            }
        }

        is Some -> "Must have a layer with no declared inputs, got ${layer.inputs}".left()
    }

    override fun makeNewActivation(activation: Activation) = "tf.keras.activations." +
        when (activation) {
            is Activation.ReLu -> "relu"
            is Activation.SoftMax -> "softmax"
            is Activation.UnknownActivation -> throw IllegalArgumentException(
                "Cannot construct an unknown activation function: $activation"
            )
        }
}
