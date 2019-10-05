package edu.wpi.axon.tfdata.layer

sealed class Activation {

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/activations/linear
     */
    object Linear : Activation()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/activations/relu
     */
    object ReLu : Activation()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/activations/softmax
     */
    object SoftMax : Activation()

    /**
     * A placeholder activation function for an activation function that Axon does not understand.
     */
    data class UnknownActivation(val name: String) : Activation()
}
