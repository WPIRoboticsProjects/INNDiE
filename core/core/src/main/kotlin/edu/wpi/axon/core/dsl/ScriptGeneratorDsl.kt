package edu.wpi.axon.core.dsl

import kotlin.reflect.KClass

@SuppressWarnings("UseDataClass")
@AxonDslMarker
class ScriptGeneratorDsl(
    val variables: PolymorphicDomainObjectContainer<Variable>,
    configure: ScriptGeneratorDsl.() -> Unit
) {

    init {
        configure()
    }
}

fun <T : Any, U : T> PolymorphicDomainObjectContainer<T>.creating(
    type: KClass<U>,
    configuration: U.() -> Unit
) = PolymorphicDomainObjectContainerDelegateProvider.of(this, type, configuration)
