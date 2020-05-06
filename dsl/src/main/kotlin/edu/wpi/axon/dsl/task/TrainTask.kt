package edu.wpi.axon.dsl.task

import arrow.core.None
import arrow.core.Option
import arrow.core.extensions.fx
import arrow.core.getOrElse
import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Verbosity
import edu.wpi.axon.tfdata.code.pythonString
import edu.wpi.inndie.util.singleAssign

/**
 * Starts training on a model.
 */
class TrainTask(name: String) : BaseTask(name) {

    /**
     * The model to train.
     */
    var modelInput: Variable by singleAssign()

    /**
     * The input data.
     */
    var trainInputData: Variable by singleAssign()

    /**
     * The target data.
     */
    var trainOutputData: Variable by singleAssign()

    /**
     * The validation split. Overridden by specifying [validationInputData] and
     * [validationOutputData].
     */
    var validationSplit: Double = 0.0

    /**
     * The input data for validation.
     */
    var validationInputData: Option<Variable> = None

    /**
     * The target data for validation.
     */
    var validationOutputData: Option<Variable> = None

    /**
     * Any callbacks (e.x., saving checkpoints).
     */
    var callbacks: Set<Variable> = setOf()

    /**
     * The number of samples per gradient update. Do not specify this (i.e., make it `null`) if the
     * data is in the form of symbolic tensors, dataset, dataset iterators, generators, or
     * `keras.utils.Sequence` instances.
     */
    var batchSize: Int? = null

    /**
     * The number of epochs to train for.
     */
    var epochs: Int = 1

    /**
     * The verbosity level.
     */
    var verbose: Verbosity = Verbosity.OneLinePerEpoch

    /**
     * Whether to shuffle the training data before each epoch.
     */
    var shuffle: Boolean = true

    override val imports: Set<Import> = setOf()

    override val inputs: Set<Variable>
        get() = setOf(
            modelInput,
            trainInputData,
            trainOutputData
        ) + callbacks +
            validationInputData.fold({ emptySet<Variable>() }, { setOf(it) }) +
            validationOutputData.fold({ emptySet<Variable>() }, { setOf(it) })

    override val outputs: Set<Variable> = setOf()

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    /**
     * Either both validation data fields must be defined or empty, not just one.
     */
    override fun isConfiguredCorrectly() =
        super.isConfiguredCorrectly() &&
            !(validationInputData.isDefined().xor(validationOutputData.isDefined()))

    override fun code(): String {
        val callbackString = callbacks.joinToString(prefix = "[", postfix = "]") { it.name }

        val validationString = Option.fx {
            val valX = validationInputData.bind()
            val valY = validationOutputData.bind()
            """
            |
            |    validation_data=(${valX.name}, ${valY.name}),""".trimMargin()
        }.getOrElse { "" }

        return """
            |${modelInput.name}.fit(
            |    ${trainInputData.name},
            |    ${trainOutputData.name},
            |    batch_size=${pythonString(batchSize)},
            |    epochs=$epochs,
            |    verbose=$verbose,
            |    callbacks=$callbackString,
            |    validation_split=$validationSplit,$validationString
            |    shuffle=${pythonString(shuffle)}
            |)
        """.trimMargin()
    }
}
