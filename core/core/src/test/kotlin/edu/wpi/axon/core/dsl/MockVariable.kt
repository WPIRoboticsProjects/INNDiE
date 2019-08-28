package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.variable.Variable

class MockVariable(name: String) : Variable(name) {

    override fun isConfiguredCorrectly() = true
}
