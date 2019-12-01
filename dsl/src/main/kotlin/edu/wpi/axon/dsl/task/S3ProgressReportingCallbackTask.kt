package edu.wpi.axon.dsl.task

import arrow.core.None
import arrow.core.Option
import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.UniqueVariableNameGenerator
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.code.pythonString
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject

/**
 * Reports training progress to an S3 bucket.
 */
class S3ProgressReportingCallbackTask(name: String) : BaseTask(name) {

    /**
     * The name of the model being trained.
     */
    var modelName by singleAssign<String>()

    /**
     * The name of the dataset being used in training.
     */
    var datasetName by singleAssign<String>()

    /**
     * The name of the S3 bucket to upload the progress to.
     */
    var bucketName by singleAssign<String>()

    /**
     * The region.
     */
    var region: Option<String> = None

    /**
     * Where to save the callback to.
     */
    var output: Variable by singleAssign()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf"),
        makeImport("import axon.client")
    )

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    private val variableNameGenerator: UniqueVariableNameGenerator by inject()

    override fun code(): String {
        val callbackClassName = variableNameGenerator.uniqueVariableName()
        // Add 1 to epoch because we get the index of the epoch, not the "element"
        return """
        |class $callbackClassName(tf.keras.callbacks.Callback):
        |    def on_epoch_end(self, epoch, logs=None):
        |        axon.client.impl_update_training_progress("$modelName", "$datasetName",
        |                                                  str(epoch + 1), "$bucketName",
        |                                                  ${pythonString(region)})
        |
        |${output.name} = $callbackClassName()
        """.trimMargin()
    }
}
