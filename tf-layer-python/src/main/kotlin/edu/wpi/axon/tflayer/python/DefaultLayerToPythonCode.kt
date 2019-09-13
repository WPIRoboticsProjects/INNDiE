package edu.wpi.axon.tflayer.python

import edu.wpi.axon.tflayers.Activation
import edu.wpi.axon.tflayers.Layer
import edu.wpi.axon.tflayers.SealedLayer

/**
 * An implementation of [LayerToCode] for Python.
 */
class DefaultLayerToPythonCode : LayerToCode {

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
