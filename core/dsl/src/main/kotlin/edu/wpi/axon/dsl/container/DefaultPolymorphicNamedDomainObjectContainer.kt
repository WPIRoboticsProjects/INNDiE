package edu.wpi.axon.dsl.container

import kotlin.reflect.KClass
import kotlin.reflect.full.defaultType

/**
 * A container for named domain objects. Names are given to new objects via a constructor.
 *
 * @param T The type of object in the container.
 */
class DefaultPolymorphicNamedDomainObjectContainer<T : Any>(
    private val backingMap: MutableMap<String, T>
) : PolymorphicNamedDomainObjectContainer<T>, Map<String, T> by backingMap {

    override fun <U : T> create(name: String, type: KClass<U>, configure: (U.() -> Unit)?): U {
        require(!type.isAbstract) {
            "This container cannot use abstract classes."
        }

        require(!type.isCompanion) {
            "This container cannot use companion objects."
        }

        require(backingMap.none { it.key == name }) {
            "Cannot add domain object with name $name because that name is already present."
        }

        // Find a constructor we can call to instantiate the variable
        val ctor = type.constructors.firstOrNull {
            val nonOptionalParams = it.parameters.filter { !it.isOptional }
            nonOptionalParams.size == 1 && nonOptionalParams.first().type == String::class.defaultType
        }

        require(ctor != null) {
            "Did not find a suitable constructor. There must be a constructor callable with a " +
                "String as the first and only non-optional parameter."
        }

        val obj = ctor.call(name)
        configure?.let { obj.it() }
        backingMap[name] = obj
        return obj
    }

    companion object {
        fun <T : Any> of() = DefaultPolymorphicNamedDomainObjectContainer<T>(mutableMapOf())
    }
}
