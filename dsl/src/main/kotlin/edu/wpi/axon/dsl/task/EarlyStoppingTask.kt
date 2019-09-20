package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.code.boolToPythonString
import edu.wpi.axon.tfdata.code.numberToPythonString
import edu.wpi.axon.util.singleAssign

class EarlyStoppingTask(name: String) : BaseTask(name) {

    /**
     * The quantity to be monitored.
     */
    var monitor: String = "val_loss"

    /**
     * The minimum change in the monitored quantity to qualify as an improvement.
     */
    var minDelta: Number = 0

    /**
     * The number of epochs with no improvement after which training will be stopped.
     */
    var patience: Int = 0

    /**
     * The verbosity.
     */
    var verbose: Int = 0

    /**
     * One of `auto`, `min`, `max`. In `min` mode, training will stop when the quantity monitored
     * has stopped decreasing; in `max` mode it will stop when the quantity monitored has stopped
     * increasing; in `auto` mode, the direction is automatically inferred from the name of the
     * monitored quantity.
     */
    var mode: String = "auto"

    /**
     * A baseline value for the monitored quantity. Training will stop if the model doesn't show
     * improvement over the baseline.
     */
    var baseline: Number? = null

    /**
     * Whether to restore model weights from the epoch with the best value of the monitored
     * quantity.
     */
    var restoreBestWeights: Boolean = false

    /**
     * Where to save the callback to.
     */
    var output: Variable by singleAssign()

    override val imports: Set<Import> = setOf(makeImport("import tensorflow as tf"))

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: Set<Code<*>> = setOf()

    override fun code() = """
        |${output.name} = tf.keras.callbacks.EarlyStopping(
        |    monitor="$monitor",
        |    min_delta=$minDelta,
        |    patience=$patience,
        |    verbose=$verbose,
        |    mode="$mode",
        |    baseline=${numberToPythonString(baseline)},
        |    restore_best_weights=${boolToPythonString(restoreBestWeights)}
        |)
    """.trimMargin()
}
