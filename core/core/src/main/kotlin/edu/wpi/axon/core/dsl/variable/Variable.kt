package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Configurable
import edu.wpi.axon.core.dsl.validator.variablename.VariableNameValidator
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * A [Variable] is literally a variable in the generated code.
 *
 * @param name The name of this [Variable] (this will become the name in the code).
 */
open class Variable(val name: String) : Configurable, KoinComponent {

    /**
     * Validates the variable name.
     */
    val variableNameValidator: VariableNameValidator by inject()

    override fun isConfiguredCorrectly() = variableNameValidator.isValidVariableName(name)
}
