package edu.wpi.inndie.dsl

import edu.wpi.inndie.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.inndie.dsl.container.PolymorphicNamedDomainObjectContainerDelegateProvider
import edu.wpi.inndie.dsl.task.Task
import edu.wpi.inndie.dsl.variable.Variable
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

/**
 * Creates a new task and configures it. Only one task of this [type] will ever be created in
 * the scope of this container.
 */
fun <T : Task, U : T> PolymorphicNamedDomainObjectContainer<T>.runExactlyOnce(
    type: KClass<U>,
    configuration: (U.() -> Unit)? = null
): U {
    @Suppress("UNCHECKED_CAST")
    return entries.firstOrNull { it.value::class == type }?.value as U? ?: run(type, configuration)
}
