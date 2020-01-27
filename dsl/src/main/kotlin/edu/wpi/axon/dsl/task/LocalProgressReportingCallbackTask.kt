package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.UniqueVariableNameGenerator
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.createProgressFilePath
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject

/**
 * Reports training progress to a local file.
 */
class LocalProgressReportingCallbackTask(name: String) : BaseTask(name) {

    /**
     * The name of the model being trained.
     */
    var modelName by singleAssign<String>()

    /**
     * The name of the dataset being used in training.
     */
    var datasetName by singleAssign<String>()

    /**
     * Where to save the callback to.
     */
    var output: Variable by singleAssign()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf"),
        makeImport("import os"),
        makeImport("from pathlib import Path"),
        makeImport("import errno")
    )

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    private val variableNameGenerator: UniqueVariableNameGenerator by inject()

    override fun code(): String {
        val callbackClassName = variableNameGenerator.uniqueVariableName()
        val progressFilePath = createProgressFilePath(modelName, datasetName)

        // Add 1 to epoch because we get the index of the epoch, not the "element"
        return """
        |class $callbackClassName(tf.keras.callbacks.Callback):
        |    def __init__(self):
        |        super()
        |        try:
        |            os.makedirs(Path("$progressFilePath").parent)
        |        except OSError as err:
        |            if err.errno != errno.EEXIST:
        |                raise
        |
        |    def on_epoch_end(self, epoch, logs=None):
        |        with open("$progressFilePath", "w") as f:
        |            f.write(str(epoch + 1))
        |
        |${output.name} = $callbackClassName()
        """.trimMargin()
    }
}