package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.task.Task

class MockTask : Task {

    override fun isConfiguredCorrectly() = true
}
