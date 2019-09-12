package edu.wpi.axon.tflayer.python

import edu.wpi.axon.tflayers.Activation
import edu.wpi.axon.tflayers.Layer

/**
 * An implementation of [LayerToCode] for Python.
 */
class DefaultLayerToPythonCode : LayerToCode {

    override fun makeNewLayer(layer: Layer) = when (layer) {
        is Layer.Dense -> """tf.keras.layers.Dense(name="${layer.name}", """ +
            "trainable=${boolToPythonString(layer.trainable)}, " +
            "units=${layer.units}, " +
            "activation=${makeNewActivation(layer.activation)})"

        is Layer.UnknownLayer ->
            throw IllegalArgumentException("Cannot construct an unknown layer: $layer")
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
