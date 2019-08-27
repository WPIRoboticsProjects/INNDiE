package edu.wpi.axon.core.dsl.container

import kotlin.reflect.KClass

interface PolymorphicNamedDomainObjectContainer<T : Any> : Collection<T> {

    fun <U : T> create(name: String, type: KClass<U>, configure: (U.() -> Unit)? = null): U
}
