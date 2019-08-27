package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.VariableNameValidator
import edu.wpi.axon.core.dsl.variable.Variable

class Yolov3PostprocessOutput(name: String, variableNameValidator: VariableNameValidator) :
    Variable(name, variableNameValidator)

class YoloV3PostprocessTask : Task {

    var input: Variable? = null
    var output: Variable? = null

    override fun isConfiguredCorrectly(): Boolean {
        TODO("not implemented")
    }
}
