package edu.wpi.axon.core.dsl.container

import kotlin.reflect.KProperty

class DslDelegate<T>
private constructor(
    internal val delegate: T
) {

    operator fun getValue(receiver: Any?, property: KProperty<*>): T = delegate

    companion object {
        fun <T> of(delegate: T) = DslDelegate(delegate)
    }
}
