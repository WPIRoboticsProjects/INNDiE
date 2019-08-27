package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.variable.InferenceSession
import edu.wpi.axon.core.dsl.variable.InputData
import edu.wpi.axon.core.dsl.variable.Variable

class InferenceTaskOutput(name: String) : Variable(name) {

    override val imports: Set<Import> = emptySet()
}

class InferenceTask : Task {

    var input: InputData? = null
    var inferenceSession: InferenceSession? = null
    var output: InferenceTaskOutput? = null

    override val imports: Set<Import> = setOf(Import.ModuleOnly("onnx"))

    override fun isConfiguredCorrectly() =
        input != null && inferenceSession != null && output != null
}
