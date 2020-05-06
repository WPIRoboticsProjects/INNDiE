package edu.wpi.inndie.tfdata.layer

import edu.wpi.inndie.tfdata.SerializableEitherDLd
import kotlinx.serialization.Serializable

/**
 * Methods to initialize weights.
 */
@Serializable
sealed class Initializer {

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/initializers/constant
     */
    @Serializable
    data class Constant(
        val value: SerializableEitherDLd = SerializableEitherDLd.Left(0.0)
    ) : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/initializers/identity
     */
    @Serializable
    data class Identity(
        val gain: Double = 1.0
    ) : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/initializers/ones
     */
    @Serializable
    object Ones : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/initializers/orthogonal
     */
    @Serializable
    data class Orthogonal(
        val gain: Double = 1.0,
        val seed: Int? = null
    ) : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/initializers/RandomNormal
     */
    @Serializable
    data class RandomNormal(
        val mean: Double = 0.0,
        val stddev: Double = 0.05,
        val seed: Int? = null
    ) : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/initializers/RandomUniform
     */
    @Serializable
    data class RandomUniform(
        val minVal: SerializableEitherDLd = SerializableEitherDLd.Left(-0.05),
        val maxVal: SerializableEitherDLd = SerializableEitherDLd.Left(0.05),
        val seed: Int? = null
    ) : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/initializers/TruncatedNormal
     */
    @Serializable
    data class TruncatedNormal(
        val mean: Double = 0.0,
        val stddev: Double = 0.05,
        val seed: Int? = null
    ) : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/initializers/variance_scaling
     */
    @Serializable
    data class VarianceScaling(
        val scale: Double = 1.0,
        val mode: Mode = Mode.FanIn,
        val distribution: Distribution = Distribution.TruncatedNormal,
        val seed: Int? = null
    ) : Initializer() {
        enum class Mode(val value: String) {
            FanIn("fan_in"), FanOut("fan_out"), FanAvg("fan_avg")
        }

        enum class Distribution(val value: String) {
            Normal("normal"), Uniform("uniform"), TruncatedNormal("truncated_normal"),
            UntruncatedNormal("untruncated_normal")
        }
    }

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/zeros_initializer
     */
    @Serializable
    object Zeros : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/glorot_uniform_initializer
     */
    @Serializable
    data class GlorotNormal(
        val seed: Int? = null
    ) : Initializer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/glorot_uniform_initializer
     */
    @Serializable
    data class GlorotUniform(
        val seed: Int? = null
    ) : Initializer()
}
