package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.variable.Variable

class Yolov3PostprocessOutput(name: String) : Variable(name) {

    override val imports: Set<Import> = emptySet()
}
