package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.variable.InputData
import edu.wpi.axon.core.dsl.variable.Variable

/**
 * A [Task] that post-processes the output from a YoloV3 model.
 */
class YoloV3PostprocessTask : Task {

    /**
     * The input data, typically an [InferenceTaskOutput].
     */
    var input: Variable? = null

    /**
     * The variable to output to.
     */
    var output: Variable? = null

    override val imports: Set<Import> = setOf(
        Import.ModuleAndIdentifier("axon", "postprocessYolov3")
    )

    override val inputData: Set<InputData>
        get() = emptySet()

    override fun isConfiguredCorrectly() = input != null && output != null

    override fun code() = """
        |${output!!.name} = postprocessYolov3(${input!!.name})
    """.trimMargin()
}
