package edu.wpi.axon.tfdata.layer

import kotlinx.serialization.Serializable

@Serializable
sealed class Activation {

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/activations/linear
     */
    @Serializable
    object Linear : Activation()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/activations/relu
     */
    @Serializable
    object ReLu : Activation()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/activations/softmax
     */
    @Serializable
    object SoftMax : Activation()

    /**
     * A placeholder activation function for an activation function that Axon does not understand.
     */
    @Serializable
    data class UnknownActivation(val name: String) : Activation()
}
