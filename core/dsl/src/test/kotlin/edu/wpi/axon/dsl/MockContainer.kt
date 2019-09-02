package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer
import org.junit.jupiter.api.fail
import kotlin.reflect.KClass

class MockContainer<T : Any>(
    val backingMap: MutableMap<String, T>
) : PolymorphicNamedDomainObjectContainer<T>, Map<String, T> by backingMap {
    override fun <U : T> create(name: String, type: KClass<U>, configure: (U.() -> Unit)?): U {
        fail { "create is not implemented." }
    }
}

@Suppress("UNCHECKED_CAST")
fun mockContainer(
    backingMap: MutableMap<String, out Any>
): PolymorphicNamedDomainObjectContainer<AnyCode> = when (backingMap.values.first()) {
    is AnyCode -> MockContainer(backingMap) as PolymorphicNamedDomainObjectContainer<AnyCode>
    else -> fail { "Unknown map value type." }
}
