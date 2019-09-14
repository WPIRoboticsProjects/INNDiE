package edu.wpi.axon.tfdata.code

import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.layer.SealedLayer

class DefaultLayerToCode : LayerToCode {

    override fun makeNewLayer(layer: Layer): String = when (layer) {
        is SealedLayer.MetaLayer -> makeNewLayer(layer.layer)

        is SealedLayer.Dense -> """tf.keras.layers.Dense(name="${layer.name}", """ +
            "units=${layer.units}, " +
            "activation=${makeNewActivation(layer.activation)})"

        else -> throw IllegalArgumentException("Cannot construct an unknown layer: $layer")
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
