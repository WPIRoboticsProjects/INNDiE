package edu.wpi.axon.core.dsl

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class PolymorphicDomainObjectContainerDelegateProvider<T : Any, U : T>
private constructor(
    internal val container: PolymorphicDomainObjectContainer<T>,
    internal val type: KClass<U>,
    internal val configuration: (U.() -> Unit)? = null
) {
    companion object {
        fun <T : Any, U : T> of(
            container: PolymorphicDomainObjectContainer<T>,
            type: KClass<U>,
            configuration: (U.() -> Unit)? = null
        ) = PolymorphicDomainObjectContainerDelegateProvider(container, type, configuration)
    }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): DslDelegate<U> =
        DslDelegate.of(
            when (configuration) {
                null -> container.create(property.name, type)
                else -> container.create(property.name, type, configuration)
            }
        )
}
