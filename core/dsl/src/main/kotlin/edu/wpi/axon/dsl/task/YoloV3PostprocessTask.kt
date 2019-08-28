package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Import
import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.variable.Variable

/**
 * A [Task] that post-processes the output from a YoloV3 model.
 */
class YoloV3PostprocessTask(name: String) : Task(name) {

    /**
     * The input data, typically the output of [InferenceTask].
     */
    var input: Variable? = null

    /**
     * The variable to output to.
     */
    var output: Variable? = null

    override val imports
        get() = (dependencies.flatMapTo(mutableSetOf()) { it.imports } +
            Import.ModuleAndIdentifier("axon", "postprocessYolov3")).toSet()

    override val inputs: Set<Variable>
        get() = setOf(input!!)

    override val outputs: Set<Variable>
        get() = setOf(output!!)

    override val dependencies: Set<Code<*>>
        get() = emptySet()

    override fun isConfiguredCorrectly() = input != null && output != null

    override fun code() = """
        |${output!!.name} = postprocessYolov3(${input!!.name})
    """.trimMargin()
}
