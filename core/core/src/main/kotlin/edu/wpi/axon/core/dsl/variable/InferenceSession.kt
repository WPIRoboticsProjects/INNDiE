package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.PathValidator
import edu.wpi.axon.core.dsl.VariableNameValidator

class InferenceSession(
    name: String,
    variableNameValidator: VariableNameValidator,
    private val pathValidator: PathValidator
) : Variable(name, variableNameValidator) {

    var modelPath: String = ""

    override fun isConfiguredCorrectly() =
        super.isConfiguredCorrectly() && pathValidator.isValidPathName(modelPath)
}
