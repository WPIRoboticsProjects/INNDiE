package edu.wpi.axon.tasks.yolov3

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.task.BaseTask
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign

/**
 * Post-processes the output from a YoloV3 model.
 */
class YoloV3PostprocessTask(name: String) : BaseTask(name) {

    /**
     * The input data, typically the output of [InferenceTask].
     */
    var input: Variable by singleAssign()

    /**
     * The variable to output to.
     */
    var output: Variable by singleAssign()

    override val imports
        get() = (dependencies.flatMapTo(mutableSetOf()) { it.imports } +
            Import.ModuleAndIdentifier("axon", "postprocessYolov3")).toSet()

    override val inputs: Set<Variable>
        get() = setOf(input)

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: Set<Code<*>>
        get() = emptySet()

    override fun code() = """
        |${output.name} = postprocessYoloV3(${input.name})
    """.trimMargin()
}
