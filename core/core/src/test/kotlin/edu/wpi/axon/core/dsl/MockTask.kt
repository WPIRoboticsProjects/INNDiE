package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.task.Task
import edu.wpi.axon.core.dsl.variable.Code
import java.util.concurrent.CountDownLatch

class MockTask(override val name: String) : Task {

    override val imports: Set<Import> = emptySet()
    override val dependencies: MutableSet<Code> = mutableSetOf()
    var latch: CountDownLatch? = null

    override fun isConfiguredCorrectly() = true

    override fun code() = "".also { latch?.countDown() }
}
