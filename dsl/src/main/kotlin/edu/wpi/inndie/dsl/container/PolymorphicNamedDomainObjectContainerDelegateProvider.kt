package edu.wpi.inndie.dsl.container

import edu.wpi.inndie.dsl.UniqueVariableNameGenerator
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Provides a delegate when adding a new named domain object. The name of the new object is derived
 * from the [UniqueVariableNameGenerator].
 *
 * @param T The type of the domain objects in the [container].
 * @param U The type of the object being created.
 * @param container The container to add the object to.
 * @param type The type of the object.
 * @param configuration Configures the object after it is created.
 */
class PolymorphicNamedDomainObjectContainerDelegateProvider<T : Any, U : T>
private constructor(
    private val container: PolymorphicNamedDomainObjectContainer<T>,
    private val type: KClass<U>,
    private val configuration: (U.() -> Unit)? = null
) : KoinComponent {

    private val uniqueVariableNameGenerator: UniqueVariableNameGenerator by inject()

    companion object {
        fun <T : Any, U : T> of(
            container: PolymorphicNamedDomainObjectContainer<T>,
            type: KClass<U>,
            configuration: (U.() -> Unit)? = null
        ) = PolymorphicNamedDomainObjectContainerDelegateProvider(container, type, configuration)
    }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): DslDelegate<U> =
        DslDelegate.of(
            container.create(
                uniqueVariableNameGenerator.uniqueVariableName(),
                type,
                configuration
            )
        )
}
