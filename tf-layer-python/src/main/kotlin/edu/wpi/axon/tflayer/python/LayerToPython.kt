package edu.wpi.axon.tflayer.python

import edu.wpi.axon.tflayers.Activation
import edu.wpi.axon.tflayers.Layer

/**
 * Get the Python code for a [layer].
 *
 * @param layer The [Layer].
 * @return The Python code.
 */
fun makeNewLayerPython(layer: Layer) = when (layer) {
    is Layer.Dense -> """tf.keras.layers.Dense(name="${layer.name}", """ +
        "trainable=${boolToPythonString(layer.trainable)}, " +
        "units=${layer.units}, " +
        "activation=${makeNewActivationPython(layer.activation)})"

    is Layer.UnknownLayer ->
        throw IllegalArgumentException("Cannot construct an unknown layer: $layer")
}

/**
 * Get the Python code for an [activation].
 *
 * @param activation The [Activation].
 * @return The Python code.
 */
fun makeNewActivationPython(activation: Activation) = "tf.keras.activations." +
    when (activation) {
        is Activation.ReLu -> "relu"
        is Activation.SoftMax -> "softmax"
        is Activation.UnknownActivation -> throw IllegalArgumentException(
            "Cannot construct an unknown activation function: $activation"
        )
    }

fun boolToPythonString(bool: Boolean): String = if (bool) "True" else "False"
