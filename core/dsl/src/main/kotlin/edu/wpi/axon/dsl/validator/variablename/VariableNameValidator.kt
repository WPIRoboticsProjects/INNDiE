package edu.wpi.axon.dsl.validator.variablename

/**
 * Validates variable names.
 */
interface VariableNameValidator {

    /**
     * Validates a variable name.
     *
     * @param name The variable name.
     * @return True if the [name] is valid.
     */
    fun isValidVariableName(name: String): Boolean
}
