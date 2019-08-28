package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.task.Task
import edu.wpi.axon.core.dsl.variable.Code
import edu.wpi.axon.core.dsl.variable.Variable
import java.util.concurrent.CountDownLatch

class MockTask(override val name: String) : Task {

    override val imports: Set<Import> = emptySet()
    override val inputs: MutableSet<Variable> = mutableSetOf()
    override val outputs: MutableSet<Variable> = mutableSetOf()
    override val dependencies: MutableSet<Code<Code<*>>> = mutableSetOf()
    var latch: CountDownLatch? = null

    override fun isConfiguredCorrectly() = true

    override fun code() = "".also { latch?.countDown() }

    override fun toString() = "MockTask(name='$name')"
}
