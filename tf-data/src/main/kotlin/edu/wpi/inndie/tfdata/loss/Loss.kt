package edu.wpi.inndie.tfdata.loss

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

@Serializable
sealed class Loss {

    @Serializable
    object CategoricalCrossentropy : Loss()

    @Serializable
    object SparseCategoricalCrossentropy : Loss()

    @Serializable
    object MeanSquaredError : Loss()

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
