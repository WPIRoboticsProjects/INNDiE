package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Configurable
import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.validator.variablename.VariableNameValidator
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class Variable(val name: String) : Configurable, KoinComponent {

    val variableNameValidator: VariableNameValidator by inject()

    abstract val imports: Set<Import>

    override fun isConfiguredCorrectly() = variableNameValidator.isValidVariableName(name)
}
