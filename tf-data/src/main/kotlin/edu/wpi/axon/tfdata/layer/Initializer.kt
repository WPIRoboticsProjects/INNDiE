package edu.wpi.axon.tfdata.layer

import arrow.core.Either
import arrow.core.Left

/**
 * Methods to initialize weights.
 *
 * TODO: https://github.com/wpilibsuite/Axon/issues/90
 */
sealed class Initializer {

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/initializers/constant
     */
    data class Constant(
        val value: Either<Double, List<Double>>
    ) : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/initializers/identity
     */
    data class Identity(
        val gain: Double = 1.0
    ) : Initializer()

    object Normal : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/initializers/ones
     */
    object Ones : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/initializers/orthogonal
     */
    data class Orthogonal(
        val gain: Double = 1.0,
        val seed: Int? = null
    ) : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/initializers/RandomNormal
     */
    data class RandomNormal(
        val mean: Double = 0.0,
        val stddev: Double = 0.05,
        val seed: Int? = null
    ) : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/initializers/RandomUniform
     */
    data class RandomUniform(
        val minVal: Either<Double, List<Double>> = Left(-0.05),
        val maxVal: Either<Double, List<Double>> = Left(0.05),
        val seed: Int? = null
    ) : Initializer()

    object TruncatedNormal : Initializer()
    object Uniform : Initializer()
    object VarianceScaling : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/zeros_initializer
     */
    object Zeros : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/glorot_uniform_initializer
     */
    data class GlorotNormal(
        val seed: Int? = null
    ) : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/glorot_uniform_initializer
     */
    data class GlorotUniform(
        val seed: Int? = null
    ) : Initializer()
}
