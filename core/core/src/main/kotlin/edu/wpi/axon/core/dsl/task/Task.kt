package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.Configurable
import edu.wpi.axon.core.dsl.Import

interface Task : Configurable {

    val imports: Set<Import>
}
