package edu.wpi.axon.core.dsl.container

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Provides a delegate when adding a new nameless domain object.
 *
 * @param T The type of the domain objects in the [container].
 * @param U The type of the object being created.
 * @param container The container to add the object to.
 * @param type The type of the object.
 * @param configuration Configures the object after it is created.
 */
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
        DslDelegate.of(container.create(type, configuration))
}
