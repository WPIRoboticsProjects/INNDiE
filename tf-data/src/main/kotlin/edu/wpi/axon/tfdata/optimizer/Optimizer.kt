package edu.wpi.axon.tfdata.optimizer

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule

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
        JsonConfiguration.Stable,
        context = optimizerModule
    ).stringify(PolymorphicWrapper.serializer(), PolymorphicWrapper(this))

    companion object {
        fun deserialize(data: String): Optimizer = Json(
            JsonConfiguration.Stable,
            context = optimizerModule
        ).parse(PolymorphicWrapper.serializer(), data).wrapped
    }

    @Serializable
    private data class PolymorphicWrapper(@Polymorphic val wrapped: Optimizer)
}

val optimizerModule = SerializersModule {
    polymorphic<Optimizer> {
        addSubclass(Optimizer.Adam::class, Optimizer.Adam.serializer())
    }
}
