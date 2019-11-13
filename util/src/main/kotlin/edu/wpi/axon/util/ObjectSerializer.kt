package edu.wpi.axon.util

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.SerialClassDescImpl

/**
 * A [KSerializer] for object types.
 */
class ObjectSerializer<T : Any>(private val obj: T) : KSerializer<T> {

    override val descriptor: SerialDescriptor = SerialClassDescImpl(obj::class.simpleName!!)

    override fun deserialize(decoder: Decoder): T = obj

    override fun serialize(encoder: Encoder, obj: T) =
        encoder.beginStructure(descriptor).endStructure(descriptor)
}
