package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.variable.Variable

class MockVariable(name: String) : Variable(name) {

    override val imports: Set<Import> = emptySet()

    override fun isConfiguredCorrectly() = true
}
