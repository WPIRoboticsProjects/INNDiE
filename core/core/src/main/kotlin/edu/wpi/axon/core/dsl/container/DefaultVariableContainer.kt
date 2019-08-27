package edu.wpi.axon.core.dsl.container

import edu.wpi.axon.core.dsl.variable.Variable
import kotlin.reflect.KClass
import kotlin.reflect.full.defaultType

class DefaultVariableContainer(
    private val backingList: MutableCollection<Variable>
) : PolymorphicDomainObjectContainer<Variable>, Collection<Variable> by backingList {

    override fun <U : Variable> create(
        name: String,
        type: KClass<U>,
        configure: (U.() -> Unit)?
    ): U {
        require(!type.isAbstract) {
            "This container cannot use abstract classes."
        }

        require(!type.isCompanion) {
            "This container cannot use companion objects."
        }

        // Find a constructor we can call to instantiate the variable
        val ctor = type.constructors.firstOrNull() {
            val nonOptionalParams = it.parameters.filter { !it.isOptional }
            nonOptionalParams.size == 1 && nonOptionalParams.first().type == String::class.defaultType
        }

        require(ctor != null) {
            "Did not find a suitable constructor. There must be a constructor callable with a " +
                "String as the first and only non-optional parameter."
        }

        val obj = ctor.call(name)
        configure?.let { obj.it() }
        backingList.add(obj)
        return obj
    }

    companion object {
        fun of() = DefaultVariableContainer(mutableListOf())
    }
}
