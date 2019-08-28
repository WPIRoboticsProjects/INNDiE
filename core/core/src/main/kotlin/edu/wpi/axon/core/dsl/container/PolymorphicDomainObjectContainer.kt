package edu.wpi.axon.core.dsl.container

import kotlin.reflect.KClass

/**
 * A container for nameless domain objects.
 *
 * @param T The type of object in the container.
 */
interface PolymorphicDomainObjectContainer<T : Any> : Collection<T> {

    /**
     * Creates a new domain object and adds it to the container.
     *
     * @param U The type of the object.
     * @param type The class of the object.
     * @param configure Configures the object after it has been created.
     * @return The new object.
     */
    fun <U : T> create(type: KClass<U>, configure: (U.() -> Unit)? = null): U
}
