package edu.wpi.axon.core.dsl

interface Configurable<T : Any> {

    fun isConfiguredCorrectly(): Boolean
}
