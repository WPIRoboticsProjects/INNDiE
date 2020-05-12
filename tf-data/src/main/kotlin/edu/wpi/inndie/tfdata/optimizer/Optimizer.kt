package edu.wpi.inndie.tfdata.optimizer

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

@Serializable
sealed class Optimizer {

    @Serializable
    data class Adam(
        val learningRate: Double,
        val beta1: Double,
        val beta2: Double,
        val epsilon: Double,
        val amsGrad: Boolean
    ) : Optimizer() {
        constructor() : this(
            learningRate = 0.001,
            beta1 = 0.9,
            beta2 = 0.999,
            epsilon = 1.0E-7,
            amsGrad = false
        )
    }

    @Serializable
    data class FTRL(
        val learningRate: Double,
        val learningRatePower: Double,
        val initialAccumulatorValue: Double,
        val l1RegularizationStrength: Double,
        val l2RegularizationStrength: Double,
        val l2ShrinkageRegularizationStrength: Double
    ) : Optimizer() {
        constructor() : this(
            learningRate = 0.001,
            learningRatePower = -0.5,
            initialAccumulatorValue = 0.1,
            l1RegularizationStrength = 0.0,
            l2RegularizationStrength = 0.0,
            l2ShrinkageRegularizationStrength = 0.0
        )
    }

    @Serializable
    data class RMSprop(
        val learningRate: Double,
        val rho: Double,
        val momentum: Double,
        val epsilon: Double,
        val centered: Boolean
    ) : Optimizer() {
        constructor() : this(
            learningRate = 0.001,
            rho = 0.9,
            momentum = 0.0,
            epsilon = 1e-7,
            centered = false
        )
    }

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
