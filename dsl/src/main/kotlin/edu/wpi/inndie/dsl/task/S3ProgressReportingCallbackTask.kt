package edu.wpi.inndie.dsl.task

import arrow.core.Option
import arrow.core.Some
import edu.wpi.inndie.dsl.Code
import edu.wpi.inndie.dsl.UniqueVariableNameGenerator
import edu.wpi.inndie.dsl.imports.Import
import edu.wpi.inndie.dsl.imports.makeImport
import edu.wpi.inndie.dsl.variable.Variable
import edu.wpi.inndie.util.inndieBucketName
import edu.wpi.inndie.util.singleAssign
import org.koin.core.inject
import org.koin.core.qualifier.named

/**
 * Reports training progress to an S3 bucket.
 */
class S3ProgressReportingCallbackTask(name: String) : BaseTask(name) {

    /**
     * The unique ID of the Job.
     */
    var jobId by singleAssign<Int>()

    /**
     * The CSV log file to read progress data from.
     */
    var csvLogFile by singleAssign<String>()

    /**
     * Where to save the callback to.
     */
    var output: Variable by singleAssign()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf"),
        makeImport("import axon.client"),
        makeImport("import os.path")
    )

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    /**
     * The name of the S3 bucket to upload the progress to.
     */
    private val bucketName: Option<String> by inject(named(inndieBucketName))

    private val variableNameGenerator: UniqueVariableNameGenerator by inject()

    override fun code(): String {
        require(bucketName is Some)
        val callbackClassName = variableNameGenerator.uniqueVariableName()
        // Add 1 to epoch because we get the index of the epoch, not the "element"
        return """
        |class $callbackClassName(tf.keras.callbacks.Callback):
        |    def on_epoch_end(self, epoch, logs=None):
        |        if os.path.isfile("$csvLogFile"):
        |            with open("$csvLogFile", "r") as f:
        |                axon.client.impl_update_training_progress($jobId, f.read(),
        |                                                          "${(bucketName as Some<String>).t}",
        |                                                          None)
        |
        |${output.name} = $callbackClassName()
        """.trimMargin()
    }
}
