package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.container.PolymorphicDomainObjectContainer
import edu.wpi.axon.core.dsl.container.PolymorphicDomainObjectContainerDelegateProvider
import edu.wpi.axon.core.dsl.task.Task
import edu.wpi.axon.core.dsl.variable.Variable
import kotlin.reflect.KClass

@SuppressWarnings("UseDataClass")
@AxonDslMarker
class ScriptGeneratorDsl(
    val variables: PolymorphicDomainObjectContainer<Variable>,
    val tasks: PolymorphicDomainObjectContainer<Task>,
    configure: ScriptGeneratorDsl.() -> Unit
) {

    init {
        configure()
    }
}

fun <T : Any, U : T> PolymorphicDomainObjectContainer<T>.creating(
    type: KClass<U>,
    configuration: (U.() -> Unit)? = null
) = PolymorphicDomainObjectContainerDelegateProvider.of(this, type, configuration)
