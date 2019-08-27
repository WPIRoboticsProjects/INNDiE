package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.variable.Variable

class Yolov3PostprocessOutput(name: String) : Variable(name) {

    override val imports: Set<Import> = emptySet()
}

class YoloV3PostprocessTask : Task {

    var input: Variable? = null
    var output: Variable? = null

    override val imports: Set<Import> = setOf(
        Import.ModuleAndIdentifier("axon", "postprocessYolov3")
    )

    override fun isConfiguredCorrectly(): Boolean {
        TODO("not implemented")
    }
}
