package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.Code
import edu.wpi.inndie.dsl.imports.Import
import edu.wpi.inndie.dsl.variable.Variable
import edu.wpi.inndie.util.singleAssign

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
