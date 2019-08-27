package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.task.Task
import kotlin.reflect.KClass

class DefaultTaskContainer(
    private val backingList: MutableCollection<Task>
) : PolymorphicDomainObjectContainer<Task>, Collection<Task> by backingList {

    override fun <U : Task> create(name: String, type: KClass<U>): U {
        TODO()
        // val variable = DefaultVariable(name)
        // backingList.add(variable)
        // return variable
    }

    override fun <U : Task> create(name: String, type: KClass<U>, configure: U.() -> Unit): U {
        TODO()
        // val variable = DefaultVariable(name)
        // variable.configure()
        // backingList.add(variable)
        // return variable
    }

    companion object {
        fun of() = DefaultTaskContainer(mutableListOf())
    }
}
