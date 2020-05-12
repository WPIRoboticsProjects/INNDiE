package edu.wpi.inndie.tfdata.layer

import kotlinx.serialization.Serializable

@Serializable
sealed class Constraint {

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/constraints/MaxNorm
     */
    @Serializable
    data class MaxNorm(
        val maxValue: Double = 2.0,
        val axis: Int = 0
    ) : Constraint()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/constraints/MinMaxNorm
     */
    @Serializable
    data class MinMaxNorm(
        val minValue: Double = 0.0,
        val maxValue: Double = 1.0,
        val rate: Double = 1.0,
        val axis: Int = 0
    ) : Constraint()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/constraints/NonNeg
     */
    @Serializable
    object NonNeg : Constraint()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/constraints/UnitNorm
     */
    @Serializable
    data class UnitNorm(
        val axis: Int = 0
    ) : Constraint()
}
