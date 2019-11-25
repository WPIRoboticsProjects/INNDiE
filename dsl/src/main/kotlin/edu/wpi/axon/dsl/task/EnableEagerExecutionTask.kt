package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable

/**
 * Enables TensorFlow's eager execution mode.
 */
class EnableEagerExecutionTask(name: String) : BaseTask(name) {

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf")
    )

    override val inputs: Set<Variable> = emptySet()

    override val outputs: Set<Variable> = emptySet()

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun code() = "tf.enable_eager_execution()"
}
