package edu.wpi.axon.core.dsl.container

import edu.wpi.axon.core.dsl.task.Task
import kotlin.reflect.KClass

class DefaultTaskContainer(
    private val backingList: MutableCollection<Task>
) : PolymorphicDomainObjectContainer<Task>, Collection<Task> by backingList {

    override fun <U : Task> create(type: KClass<U>, configure: (U.() -> Unit)?): U {
        require(!type.isAbstract) {
            "This container cannot use abstract classes."
        }

        require(!type.isCompanion) {
            "This container cannot use companion objects."
        }

        // Find a constructor we can call to instantiate the variable
        val ctor = type.constructors.firstOrNull() {
            val nonOptionalParams = it.parameters.filter { !it.isOptional }
            nonOptionalParams.isEmpty()
        }

        require(ctor != null) {
            "Did not find a suitable constructor. There must be a constructor callable with a " +
                "String as the first and only non-optional parameter."
        }

        val obj = ctor.call()
        configure?.let { obj.it() }
        backingList.add(obj)
        return obj
    }

    companion object {
        fun of() = DefaultTaskContainer(mutableListOf())
    }
}
