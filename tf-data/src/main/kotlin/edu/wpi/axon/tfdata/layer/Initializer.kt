package edu.wpi.axon.tfdata.layer

/**
 * Methods to initialize weights.
 *
 * TODO: https://github.com/wpilibsuite/Axon/issues/90
 */
sealed class Initializer {

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/zeros_initializer
     */
    object Zeros : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/initializers/ones
     */
    object Ones : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/glorot_uniform_initializer
     */
    data class GlorotUniform(
        val seed: Int?
    ) : Initializer()
}
