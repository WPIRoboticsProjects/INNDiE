package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainerDelegateProvider
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.variable.Variable
import kotlin.reflect.KClass

/**
 * Creates a new variable and configures it using delegation.
 */
fun <T : Variable, U : T> PolymorphicNamedDomainObjectContainer<T>.creating(
    type: KClass<U>,
    configuration: (U.() -> Unit)? = null
) = PolymorphicNamedDomainObjectContainerDelegateProvider.of(this, type, configuration)

/**
 * Creates a new variable and configures it.
 */
fun <T : Variable, U : T> PolymorphicNamedDomainObjectContainer<T>.create(
    type: KClass<U>,
    configuration: (U.() -> Unit)? = null
): U {
    val variable by PolymorphicNamedDomainObjectContainerDelegateProvider.of(
        this,
        type,
        configuration
    )
    return variable
}

/**
 * Creates a new task and configures it using delegation.
 */
fun <T : Task, U : T> PolymorphicNamedDomainObjectContainer<T>.running(
    type: KClass<U>,
    configuration: (U.() -> Unit)? = null
) = PolymorphicNamedDomainObjectContainerDelegateProvider.of(this, type, configuration)

/**
 * Creates a new task and configures it.
 */
fun <T : Task, U : T> PolymorphicNamedDomainObjectContainer<T>.run(
    type: KClass<U>,
    configuration: (U.() -> Unit)? = null
): U {
    val task by PolymorphicNamedDomainObjectContainerDelegateProvider.of(this, type, configuration)
    return task
}
