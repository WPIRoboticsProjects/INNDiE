package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.variable.Variable

class MockVariable(name: String) : Variable(name) {

    override fun isConfiguredCorrectly() = true
}
