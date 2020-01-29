package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign

/**
 * Inserts Python's slice notation to slice the [input].
 */
class SliceTask(name: String) : BaseTask(name) {

    /**
     * The input.
     */
    var input: Variable by singleAssign()

    /**
     * The output.
     */
    var output: Variable by singleAssign()

    var sliceNotation: String by singleAssign()

    override val imports: Set<Import> = setOf()

    override val inputs: Set<Variable>
        get() = setOf(input)

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun code(): String = "${output.name} = ${input.name}$sliceNotation"
}
