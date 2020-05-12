package edu.wpi.inndie.dsl

/**
 * Returns variable names like `var1`, `var2`, etc. with synchronized access.
 */
class DefaultUniqueVariableNameGenerator :
    UniqueVariableNameGenerator {

    private var count = 1

    override fun uniqueVariableName() = synchronized(this) { "var${count++}" }
}
