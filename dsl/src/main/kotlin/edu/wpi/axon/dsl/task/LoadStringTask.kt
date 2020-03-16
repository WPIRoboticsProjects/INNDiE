package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign

/**
 * Puts a string into a variable.
 */
class LoadStringTask(name: String) : BaseTask(name) {

    /**
     * The string to load into the output.
     */
    var data by singleAssign<String>()

    /**
     * The variable to write to.
     */
    var output by singleAssign<Variable>()

    override val imports: Set<Import> = setOf()

    override val inputs: Set<Variable> = emptySet()

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun code() = """${output.name} = "$data""""
}
