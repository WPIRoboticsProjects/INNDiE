package edu.wpi.axon.tflayers

sealed class Activation {

    /**
     * The Rectified Linear Unit activation function.
     */
    object ReLu : Activation()

    /**
     * The softmax activation function.
     */
    object SoftMax : Activation()

    /**
     * A placeholder activation function for an activation function that Axon does not understand.
     */
    data class UnknownActivation(val name: String) : Activation()
}
