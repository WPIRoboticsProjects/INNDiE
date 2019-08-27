package edu.wpi.axon.core.dsl

import kotlin.reflect.KClass

interface PolymorphicDomainObjectContainer<T : Any> : Collection<T> {

    fun <U : T> create(name: String, type: KClass<U>, configure: (U.() -> Unit)? = null): U
}
