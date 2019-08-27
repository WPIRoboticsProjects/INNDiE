package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.task.Task

class MockTask : Task {

    override val imports: Set<Import> = emptySet()

    override fun isConfiguredCorrectly() = true
}
