package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.variable.InputData
import edu.wpi.axon.core.dsl.variable.Variable

class YoloV3PostprocessTask : Task {

    var input: Variable? = null
    override var output: Variable? = null

    override val imports: Set<Import> = setOf(
        Import.ModuleAndIdentifier("axon", "postprocessYolov3")
    )

    override val inputVariables: Set<Variable>
        get() = setOf(input!!)
    override val inputData: Set<InputData>
        get() = emptySet()

    override fun isConfiguredCorrectly() = input != null && output != null

    override fun code() = """
        |${output!!.name} = postprocessYolov3(${input!!.name})
    """.trimMargin()
}
