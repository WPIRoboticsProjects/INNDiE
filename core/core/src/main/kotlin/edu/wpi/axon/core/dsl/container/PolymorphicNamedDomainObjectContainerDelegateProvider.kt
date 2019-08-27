package edu.wpi.axon.core.dsl.container

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class PolymorphicNamedDomainObjectContainerDelegateProvider<T : Any, U : T>
private constructor(
    internal val container: PolymorphicNamedDomainObjectContainer<T>,
    internal val type: KClass<U>,
    internal val configuration: (U.() -> Unit)? = null
) {
    companion object {
        fun <T : Any, U : T> of(
            container: PolymorphicNamedDomainObjectContainer<T>,
            type: KClass<U>,
            configuration: (U.() -> Unit)? = null
        ) = PolymorphicNamedDomainObjectContainerDelegateProvider(container, type, configuration)
    }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): DslDelegate<U> =
        DslDelegate.of(container.create(property.name, type, configuration))
}
