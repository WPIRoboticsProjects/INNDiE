package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Import
import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.variable.InferenceSession
import edu.wpi.axon.dsl.variable.ModelInputData
import edu.wpi.axon.dsl.variable.Variable

/**
 * A [Task] that runs inference.
 */
class InferenceTask(name: String) : Task(name) {

    /**
     * The data input to the first layer of the model.
     */
    var input: ModelInputData? = null

    /**
     * The session to give inputs to, run, and get outputs from.
     */
    var inferenceSession: InferenceSession? = null

    /**
     * The variable to output to.
     */
    var output: Variable? = null

    override val imports
        get() = (dependencies.flatMapTo(mutableSetOf()) { it.imports } +
            Import.ModuleOnly("onnx")).toSet()

    override val inputs: Set<Variable>
        get() = emptySet()

    override val outputs: Set<Variable>
        get() = setOf(output!!)

    override val dependencies: Set<Code<*>>
        get() = setOf(input!!, inferenceSession!!)

    override fun isConfiguredCorrectly() =
        input != null && inferenceSession != null && output != null

    override fun code(): String {
        val sessionInputsName = "sessionInputNames"
        val modelInput = input!!.codeForModelInput(sessionInputsName)
        return """
            |$sessionInputsName = ${inferenceSession!!.name}.get_inputs()
            |${output!!.name} = ${inferenceSession!!.name}.run(None, $modelInput)
        """.trimMargin()
    }
}
