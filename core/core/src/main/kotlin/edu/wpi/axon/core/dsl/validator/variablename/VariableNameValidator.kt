package edu.wpi.axon.core.dsl.validator.variablename

interface VariableNameValidator {

    fun isValidVariableName(name: String): Boolean
}
