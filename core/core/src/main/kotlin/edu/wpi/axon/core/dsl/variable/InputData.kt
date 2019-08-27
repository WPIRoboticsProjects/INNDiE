package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.PathValidator
import edu.wpi.axon.core.dsl.VariableNameValidator

abstract class InputData(
    name: String,
    variableNameValidator: VariableNameValidator,
    private val pathValidator: PathValidator
) : Variable(name, variableNameValidator) {

    var path: String? = null

    override fun isConfiguredCorrectly() =
        super.isConfiguredCorrectly() && path != null && pathValidator.isValidPathName(path!!)
}
