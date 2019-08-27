package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.variable.Variable
import io.mockk.mockk

class MockVariable(name: String) : Variable(name, mockk()) {

    override fun isConfiguredCorrectly() = true
}
