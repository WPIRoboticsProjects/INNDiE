package edu.wpi.axon.tflayers

sealed class Layer(open val name: String, open val trainable: Boolean) {

    /**
     * A placeholder layer for a layer that Axon does not understand.
     */
    object UnknownLayer : Layer("unknownLayer", false)
}
