package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign

/**
 * Runs inference using the ONNX runtime.
 */
class InferenceTask(name: String) : BaseTask(name) {

    /**
     * The data input to the first layer of the model.
     */
    var input: Variable by singleAssign()

    /**
     * The session to give inputs to, run, and get outputs from.
     */
    var inferenceSession: Variable by singleAssign()

    /**
     * The variable to output to.
     */
    var output: Variable by singleAssign()

    override val imports: Set<Import> = setOf(Import.ModuleOnly("onnx"))

    override val inputs: Set<Variable>
        get() = setOf(input, inferenceSession)

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: Set<Code<*>>
        get() = setOf()

    override fun code() = """
        |${output.name} = ${inferenceSession.name}.run(None, ${input.name})
    """.trimMargin()
}
