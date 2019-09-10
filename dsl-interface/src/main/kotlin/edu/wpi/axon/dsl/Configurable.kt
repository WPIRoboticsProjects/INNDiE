package edu.wpi.axon.dsl

/**
 * A "typeclass" for anything that can be configured.
 */
interface Configurable {

    /**
     * @return Whether this component is configured correctly.
     */
    fun isConfiguredCorrectly(): Boolean
}
