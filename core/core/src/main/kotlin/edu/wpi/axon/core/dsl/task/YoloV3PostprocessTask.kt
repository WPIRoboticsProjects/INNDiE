package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.variable.FileInputData
import edu.wpi.axon.core.dsl.variable.Variable

/**
 * A [Task] that post-processes the output from a YoloV3 model.
 */
class YoloV3PostprocessTask(override val name: String) : Task {

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

    override val dependencies: Set<FileInputData>
        get() = emptySet()

    override fun isConfiguredCorrectly() = input != null && output != null

    override fun code() = """
        |${output!!.name} = postprocessYolov3(${input!!.name})
    """.trimMargin()
}
