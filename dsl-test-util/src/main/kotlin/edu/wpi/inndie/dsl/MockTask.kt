package edu.wpi.inndie.dsl

import edu.wpi.inndie.dsl.imports.Import
import edu.wpi.inndie.dsl.task.Task
import edu.wpi.inndie.dsl.variable.Variable
import java.util.concurrent.CountDownLatch

data class MockTask(override val name: String) : Task {

    override val imports: MutableSet<Import> = mutableSetOf()
    override val inputs: MutableSet<Variable> = mutableSetOf()
    override val outputs: MutableSet<Variable> = mutableSetOf()
    override val dependencies: MutableSet<AnyCode> = mutableSetOf()

    var latch: CountDownLatch? = null
    var code = ""
    var configuredCorrectly = true

    override fun isConfiguredCorrectly() = configuredCorrectly

    // TODO: Calling countDown in here is bad because this method should be stateless
    override fun code() = code.also { latch?.countDown() }

    override fun toString() = "MockTask(name=$name)"
}
