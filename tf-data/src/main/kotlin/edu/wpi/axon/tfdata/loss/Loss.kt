package edu.wpi.axon.tfdata.loss

import edu.wpi.axon.util.ObjectSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule

sealed class Loss {

    object SparseCategoricalCrossentropy : Loss()

    fun serialize(): String = Json(
        JsonConfiguration.Stable,
        context = lossModule
    ).stringify(PolymorphicWrapper.serializer(), PolymorphicWrapper(this))

    companion object {
        fun deserialize(data: String): Loss = Json(
            JsonConfiguration.Stable,
            context = lossModule
        ).parse(PolymorphicWrapper.serializer(), data).wrapped
    }

    @Serializable
    private data class PolymorphicWrapper(@Polymorphic val wrapped: Loss)
}

val lossModule = SerializersModule {
    polymorphic<Loss> {
        addSubclass(
            Loss.SparseCategoricalCrossentropy::class,
            ObjectSerializer(Loss.SparseCategoricalCrossentropy)
        )
    }
}
