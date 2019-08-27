package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Configurable
import edu.wpi.axon.core.dsl.VariableNameValidator

abstract class Variable(
    val name: String,
    private val variableNameValidator: VariableNameValidator
) : Configurable {

    override fun isConfiguredCorrectly() = variableNameValidator.isValidVariableName(name)
}
