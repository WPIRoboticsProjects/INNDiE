package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer
import kotlin.reflect.KClass
import org.junit.jupiter.api.fail

class MockContainer<T : Any>(
    private val backingMap: MutableMap<String, T>
) : PolymorphicNamedDomainObjectContainer<T>, Map<String, T> by backingMap {
    override fun <U : T> create(name: String, type: KClass<U>, configure: (U.() -> Unit)?): U {
        fail { "create is not implemented." }
    }
}

@Suppress("UNCHECKED_CAST")
internal fun mockContainer(
    backingMap: MutableMap<String, out Any> = mutableMapOf()
): PolymorphicNamedDomainObjectContainer<AnyCode> = when (backingMap.values.firstOrNull()) {
    is AnyCode, null -> MockContainer(backingMap) as PolymorphicNamedDomainObjectContainer<AnyCode>
    else -> fail { "Unknown map value type." }
}
