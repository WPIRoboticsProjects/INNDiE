package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.task.Task
import edu.wpi.axon.core.dsl.variable.InputData
import edu.wpi.axon.core.dsl.variable.Variable

class MockTask : Task {

    override val imports: Set<Import> = emptySet()
    override val inputVariables: Set<Variable> = emptySet()
    override val inputData: Set<InputData> = emptySet()
    override val output: Variable? = null

    override fun isConfiguredCorrectly() = true

    override fun code() = ""
}
