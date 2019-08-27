package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.Configurable
import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.variable.Code
import edu.wpi.axon.core.dsl.variable.InputData
import edu.wpi.axon.core.dsl.variable.Variable

interface Task : Configurable, Code {

    val imports: Set<Import>

    val inputVariables: Set<Variable>
    val inputData: Set<InputData>

    val output: Variable?
}
