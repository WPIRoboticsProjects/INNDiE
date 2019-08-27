package edu.wpi.axon.core.dsl

interface VariableNameValidator {

    fun isValidVariableName(name: String): Boolean
}
