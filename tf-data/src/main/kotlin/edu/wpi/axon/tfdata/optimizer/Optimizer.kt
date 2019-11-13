package edu.wpi.axon.tfdata.optimizer

import kotlinx.serialization.Serializable
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
}

val optimizerModule = SerializersModule {
    polymorphic<Optimizer> {
        addSubclass(Optimizer.Adam::class, Optimizer.Adam.serializer())
    }
}
