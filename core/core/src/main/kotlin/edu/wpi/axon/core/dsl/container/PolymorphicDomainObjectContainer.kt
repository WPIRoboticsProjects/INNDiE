package edu.wpi.axon.core.dsl.container

import kotlin.reflect.KClass

interface PolymorphicDomainObjectContainer<T : Any> : Collection<T> {

    fun <U : T> create(type: KClass<U>, configure: (U.() -> Unit)? = null): U
}
