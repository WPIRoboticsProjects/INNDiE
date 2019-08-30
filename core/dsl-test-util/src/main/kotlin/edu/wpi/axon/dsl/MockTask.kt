package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.variable.Variable
import java.util.concurrent.CountDownLatch

class MockTask(name: String) : Task(name) {

    override val imports: Set<Import> = emptySet()
    override val inputs: MutableSet<Variable> = mutableSetOf()
    override val outputs: MutableSet<Variable> = mutableSetOf()
    override val dependencies: MutableSet<AnyCode> = mutableSetOf()
    var latch: CountDownLatch? = null

    override fun isConfiguredCorrectly() = true

    // TODO: Calling countDown in here is bad because this method should be stateless
    override fun code() = "".also { latch?.countDown() }

    override fun toString() = "MockTask(name='$name')"
}
