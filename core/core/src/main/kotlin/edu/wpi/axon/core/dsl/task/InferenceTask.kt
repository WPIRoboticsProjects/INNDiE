package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.VariableNameValidator
import edu.wpi.axon.core.dsl.variable.InferenceSession
import edu.wpi.axon.core.dsl.variable.InputData
import edu.wpi.axon.core.dsl.variable.Variable

class InferenceTaskOutput(name: String, variableNameValidator: VariableNameValidator) :
    Variable(name, variableNameValidator)

class InferenceTask : Task {

    var input: InputData? = null
    var inferenceSession: InferenceSession? = null
    var output: InferenceTaskOutput? = null

    override fun isConfiguredCorrectly(): Boolean {
        TODO("not implemented")
    }
}
