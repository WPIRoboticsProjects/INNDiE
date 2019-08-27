package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.PathValidator
import edu.wpi.axon.core.dsl.VariableNameValidator

class InferenceSession(
    name: String,
    variableNameValidator: VariableNameValidator,
    private val pathValidator: PathValidator
) : Variable(name, variableNameValidator) {

    var modelPath: String? = null

    override fun isConfiguredCorrectly() =
        super.isConfiguredCorrectly() && modelPath != null &&
            pathValidator.isValidPathName(modelPath!!)
}
