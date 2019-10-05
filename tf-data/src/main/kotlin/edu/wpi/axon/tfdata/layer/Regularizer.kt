package edu.wpi.axon.tfdata.layer

sealed class Regularizer {

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/regularizers/l1
     */
    data class L1(
        val l: Double = 0.01
    ) : Regularizer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/regularizers/l2
     */
    data class L2(
        val l: Double = 0.01
    ) : Regularizer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/regularizers/L1L2
     */
    data class L1L2(
        val l1: Double = 0.01,
        val l2: Double = 0.01
    ) : Regularizer()
}
