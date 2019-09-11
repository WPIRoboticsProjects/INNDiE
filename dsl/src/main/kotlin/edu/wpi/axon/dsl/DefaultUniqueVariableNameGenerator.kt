package edu.wpi.axon.dsl

/**
 * Returns variable names like `var1`, `var2`, etc. with synchronized access.
 */
class DefaultUniqueVariableNameGenerator : UniqueVariableNameGenerator {

    private var count = 0

    override fun uniqueVariableName() = synchronized(this) { "var${count++}" }
}
