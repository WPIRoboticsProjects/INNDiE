package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign

/**
 * Casts a Tensor to a type.
 */
class CastTask(name: String) : BaseTask(name) {

    /**
     * The input.
     */
    var input: Variable by singleAssign()

    /**
     * The output.
     */
    var output: Variable by singleAssign()

    var dtype: String by singleAssign()

    override val imports: Set<Import> = setOf(makeImport("import tensorflow as tf"))

    override val inputs: Set<Variable>
        get() = setOf(input)

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun code(): String = "${output.name} = tf.cast(${input.name}, $dtype)"
}
