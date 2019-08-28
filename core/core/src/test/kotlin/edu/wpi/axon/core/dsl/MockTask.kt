package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.task.Task
import edu.wpi.axon.core.dsl.variable.FileInputData

class MockTask : Task {

    override val imports: Set<Import> = emptySet()
    override val dependencies: Set<FileInputData> = emptySet()

    override fun isConfiguredCorrectly() = true

    override fun code() = ""
}
