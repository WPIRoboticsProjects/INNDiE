package edu.wpi.axon.core.dsl.container

import kotlin.reflect.KClass

/**
 * A container for nameless domain objects. Names are given to new objects via a constructor.
 *
 * @param T The type of object in the container.
 */
interface PolymorphicNamedDomainObjectContainer<T : Any> : Collection<T> {

    /**
     * Creates a new domain object and adds it to the container.
     *
     * @param U The type of the object.
     * @param name The name of the object. This will be given to the new object via a constructor.
     * @param type The class of the object.
     * @param configure Configures the object after it has been created.
     * @return The new object.
     */
    fun <U : T> create(name: String, type: KClass<U>, configure: (U.() -> Unit)? = null): U
}
