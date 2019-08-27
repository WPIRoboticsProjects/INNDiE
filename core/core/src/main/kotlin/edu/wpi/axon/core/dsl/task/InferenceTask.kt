package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.variable.InferenceSession
import edu.wpi.axon.core.dsl.variable.InputData
import edu.wpi.axon.core.dsl.variable.ModelInputData
import edu.wpi.axon.core.dsl.variable.Variable

class InferenceTask : Task {

    var input: ModelInputData? = null
    var inferenceSession: InferenceSession? = null
    override var output: InferenceTaskOutput? = null

    override val imports: Set<Import> = setOf(Import.ModuleOnly("onnx"))

    override val inputVariables: Set<Variable>
        get() = setOf()
    override val inputData: Set<InputData>
        get() = setOf(input!!, inferenceSession!!)

    override fun isConfiguredCorrectly() =
        input != null && inferenceSession != null && output != null

    override fun code(): String {
        val sessionInputsName = "sessionInputNames"
        return """
            |$sessionInputsName = ${inferenceSession!!.name}.get_inputs()
            |${output!!.name} = ${inferenceSession!!.name}.run(None, ${input!!.codeForModelInput(sessionInputsName)})
        """.trimMargin()
    }
}
