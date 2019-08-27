package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Configurable

interface Variable : Configurable<Variable> {

    val name: String
}
