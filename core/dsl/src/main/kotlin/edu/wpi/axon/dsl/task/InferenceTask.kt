package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.Import
import edu.wpi.axon.dsl.variable.Variable

/**
 * A [Task] that runs inference.
 */
class InferenceTask(name: String) : Task(name) {

    /**
     * The data input to the first layer of the model.
     */
    var input: Variable? = null

    /**
     * The session to give inputs to, run, and get outputs from.
     */
    var inferenceSession: Variable? = null

    /**
     * The variable to output to.
     */
    var output: Variable? = null

    override val imports: Set<Import> = setOf(Import.ModuleOnly("onnx"))

    override val inputs: Set<Variable>
        get() = setOf(input!!, inferenceSession!!)

    override val outputs: Set<Variable>
        get() = setOf(output!!)

    override val dependencies: Set<Code<*>>
        get() = setOf()

    override fun isConfiguredCorrectly() = input != null && inferenceSession != null &&
        output != null && super.isConfiguredCorrectly()

    override fun code() = """
        |${output!!.name} = ${inferenceSession!!.name}.run(None, ${input!!.name})
    """.trimMargin()
}
