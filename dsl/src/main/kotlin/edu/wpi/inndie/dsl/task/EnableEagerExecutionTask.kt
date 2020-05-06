package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.Code
import edu.wpi.inndie.dsl.imports.Import
import edu.wpi.inndie.dsl.imports.makeImport
import edu.wpi.inndie.dsl.variable.Variable

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
