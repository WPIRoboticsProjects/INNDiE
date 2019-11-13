package edu.wpi.axon.tfdata.loss

import edu.wpi.axon.util.ObjectSerializer
import kotlinx.serialization.modules.SerializersModule

sealed class Loss {

    object SparseCategoricalCrossentropy : Loss()
}

val lossModule = SerializersModule {
    polymorphic<Loss> {
        addSubclass(
            Loss.SparseCategoricalCrossentropy::class,
            ObjectSerializer(Loss.SparseCategoricalCrossentropy)
        )
    }
}
