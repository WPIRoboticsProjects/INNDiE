package edu.wpi.axon.core.dsl

import kotlin.reflect.KClass

class DefaultVariableContainer(
    private val backingList: MutableCollection<Variable>
) : VariableContainer, Collection<Variable> by backingList {

    override fun <U : Variable> create(name: String, type: KClass<U>): U {
        TODO()
        // val variable = DefaultVariable(name)
        // backingList.add(variable)
        // return variable
    }

    override fun <U : Variable> create(name: String, type: KClass<U>, configure: U.() -> Unit): U {
        TODO()
        // val variable = DefaultVariable(name)
        // variable.configure()
        // backingList.add(variable)
        // return variable
    }

    companion object {
        fun of() = DefaultVariableContainer(mutableListOf())
    }
}
