package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign

/**
 * Reshapes and scales numpy arrays.
 */
class ReshapeAndScaleTask(name: String) : BaseTask(name) {

    /**
     * The input.
     */
    var input: Variable by singleAssign()

    /**
     * The output.
     */
    var output: Variable by singleAssign()

    /**
     * The arguments to reshape.
     */
    var reshapeArgs: List<Number> by singleAssign()

    /**
     * The number to scale each element by. Set to `null` to not scale.
     */
    var scale: Number? by singleAssign()

    override val imports: Set<Import> = setOf()

    override val inputs: Set<Variable>
        get() = setOf(input)

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun code(): String {
        val reshaped = "${output.name} = ${input.name}.reshape(${reshapeArgs.joinToString()})"
        return when (scale) {
            null -> reshaped
            else -> "$reshaped / $scale"
        }
    }
}
