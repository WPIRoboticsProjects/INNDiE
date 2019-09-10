package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.variable.Variable

data class EmptyBaseTask(override val name: String) : BaseTask(name) {
    override val imports: MutableSet<Import> = mutableSetOf()
    override val inputs: MutableSet<Variable> = mutableSetOf()
    override val outputs: MutableSet<Variable> = mutableSetOf()
    override val dependencies: MutableSet<Code<*>> = mutableSetOf()
    override fun code() = ""
}
