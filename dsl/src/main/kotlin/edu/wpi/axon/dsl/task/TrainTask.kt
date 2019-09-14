package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Verbosity
import edu.wpi.axon.tfdata.code.boolToPythonString
import edu.wpi.axon.tfdata.code.numberToPythonString
import edu.wpi.axon.util.singleAssign

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
     * The input data for validation.
     */
    var validationInputData: Variable by singleAssign()

    /**
     * The target data for validation.
     */
    var validationOutputData: Variable by singleAssign()

    /**
     * Any callbacks (e.x., saving checkpoints).
     */
    var callbacks: Set<Variable> = setOf()

    /**
     * The number of samples per gradient update. Do not specify this (i.e., make it `null`, if the
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
            trainOutputData,
            validationInputData,
            validationOutputData
        ) + callbacks

    override val outputs: Set<Variable>
        get() = setOf()

    override val dependencies: Set<Code<*>>
        get() = setOf()

    override fun code() = """
        |${modelInput.name}.fit(
        |    ${trainInputData.name},
        |    ${trainOutputData.name},
        |    batch_size=${numberToPythonString(batchSize)},
        |    epochs=$epochs,
        |    verbose=$verbose,
        |    callbacks=${callbacks.joinToString(prefix = "[", postfix = "]") { it.name }},
        |    validation_data=(${validationInputData.name}, ${validationOutputData.name}),
        |    shuffle=${boolToPythonString(shuffle)}
        |)
    """.trimMargin()
}
