package edu.wpi.axon.dsl

/**
 * Generates unique variables names (unique to this instance).
 */
interface UniqueVariableNameGenerator {

    /**
     * @return A new unique variable name.
     */
    fun uniqueVariableName(): String
}
