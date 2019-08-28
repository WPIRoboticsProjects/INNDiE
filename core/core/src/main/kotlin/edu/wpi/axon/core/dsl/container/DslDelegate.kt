package edu.wpi.axon.core.dsl.container

import kotlin.reflect.KProperty

/**
 * Provides an implementation for delegation.
 *
 * @param T The type of the delegate.
 * @param delegate The delegate implementation.
 */
class DslDelegate<T>
private constructor(
    private val delegate: T
) {

    operator fun getValue(receiver: Any?, property: KProperty<*>): T = delegate

    companion object {
        fun <T> of(delegate: T) = DslDelegate(delegate)
    }
}
