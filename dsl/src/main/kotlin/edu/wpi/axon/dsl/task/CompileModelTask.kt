package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.code.loss.LossToCode
import edu.wpi.axon.tfdata.code.optimizer.OptimizerToCode
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject

/**
 * Compiles a model so that it is ready for training.
 */
class CompileModelTask(name: String) : BaseTask(name) {

    /**
     * The model to compile.
     */
    var modelInput: Variable by singleAssign()

    /**
     * The optimizer to use.
     */
    var optimizer: Optimizer by singleAssign()

    /**
     * The loss to use.
     */
    var loss: Loss by singleAssign()

    /**
     * Any metrics.
     */
    var metrics: Set<String> = emptySet()

    private val optimizerToCode: OptimizerToCode by inject()
    private val lossToCode: LossToCode by inject()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf")
    )

    override val inputs: Set<Variable>
        get() = setOf(modelInput)

    override val outputs: Set<Variable> = setOf()

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun code() = """
        |${modelInput.name}.compile(
        |    optimizer=${optimizerToCode.makeNewOptimizer(optimizer)},
        |    loss=${lossToCode.makeNewLoss(loss)},
        |    metrics=${metrics.joinToString(prefix = "[", postfix = "]") { """"$it"""" }}
        |)
    """.trimMargin()
}
