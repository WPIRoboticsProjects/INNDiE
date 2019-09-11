package edu.wpi.axon.tflayers

sealed class Layer(open val name: String, open val trainable: Boolean) {

    /**
     * A placeholder layer for a layer that Axon does not understand.
     */
    data class UnknownLayer(
        override val name: String,
        override val trainable: Boolean
    ) : Layer(name, trainable)

    /**
     * A Dense layer.
     *
     * @param units The number of neurons.
     * @param activation The [Activation] function.
     */
    data class Dense(
        override val name: String,
        override val trainable: Boolean,
        val units: Int,
        val activation: Activation
    ) : Layer(name, trainable)
}
