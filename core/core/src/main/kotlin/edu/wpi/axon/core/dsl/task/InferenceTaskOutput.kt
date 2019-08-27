package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.variable.Variable

class InferenceTaskOutput(name: String) : Variable(name) {

    override val imports: Set<Import> = emptySet()
}
