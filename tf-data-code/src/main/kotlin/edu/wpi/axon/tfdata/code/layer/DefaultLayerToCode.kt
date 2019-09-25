package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.layer.SealedLayer

class DefaultLayerToCode : LayerToCode {

    override fun makeNewLayer(layer: Layer): Either<String, String> = when (layer) {
        is SealedLayer.MetaLayer -> makeNewLayer(layer.layer)

        // TODO: Implement for InputLayer using tf.keras.Input

        is SealedLayer.Dense -> ("""tf.keras.layers.Dense(name="${layer.name}", """ +
            "units=${layer.units}, " +
            "activation=${makeNewActivation(layer.activation)})").right()

        else -> "Cannot construct an unknown layer: $layer".left()
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
