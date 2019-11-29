package edu.wpi.axon.tfdata.optimizer

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
    ) : Optimizer()

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
