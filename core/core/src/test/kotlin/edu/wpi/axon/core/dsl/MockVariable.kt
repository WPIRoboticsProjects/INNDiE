package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.variable.Variable

class MockVariable(override val name: String) : Variable {

    override fun isConfiguredCorrectly() = true
}
