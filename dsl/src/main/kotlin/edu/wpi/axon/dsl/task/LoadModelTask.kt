package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.validator.path.PathValidator
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Loads a model from an HDF5 file.
 */
class LoadModelTask(name: String) : BaseTask(name), KoinComponent {

    /**
     * The file path to load the model from.
     */
    var modelPath: String by singleAssign()

    /**
     * The output the model will be stored in.
     */
    var modelOutput: Variable by singleAssign()

    /**
     * Validates the [modelPath].
     */
    private val pathValidator: PathValidator by inject()

    override val imports = setOf(makeImport("import tensorflow as tf"))

    override val inputs: Set<Variable> = emptySet()

    override val outputs: Set<Variable>
        get() = setOf(modelOutput)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun isConfiguredCorrectly() = pathValidator.isValidPathName(modelPath) &&
        super.isConfiguredCorrectly()

    override fun code() = """
        |${modelOutput.name} = tf.keras.models.load_model("$modelPath")
    """.trimMargin()
}
