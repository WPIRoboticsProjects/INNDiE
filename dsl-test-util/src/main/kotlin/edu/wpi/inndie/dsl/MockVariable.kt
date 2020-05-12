package edu.wpi.inndie.dsl

import edu.wpi.inndie.dsl.variable.Variable

class MockVariable(name: String) : Variable(name) {

    override fun isConfiguredCorrectly() = true
}
