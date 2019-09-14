package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.code.DatasetToCode
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject

/**
 * Loads one of the example datasets.
 */
class LoadExampleDatasetTask(name: String) : BaseTask(name) {

    /**
     * The dataset to load.
     */
    var dataset: Dataset by singleAssign()

    /**
     * The input data for training.
     */
    var xTrainOutput: Variable by singleAssign()

    /**
     * The target data for training.
     */
    var yTrainOutput: Variable by singleAssign()

    /**
     * The input data for validation.
     */
    var xTestOutput: Variable by singleAssign()

    /**
     * The target data for validation.
     */
    var yTestOutput: Variable by singleAssign()

    private val datasetToCode: DatasetToCode by inject()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf")
    )

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable>
        get() = setOf(xTrainOutput, yTrainOutput, xTestOutput, yTestOutput)

    override val dependencies: Set<Code<*>>
        get() = setOf()

    override fun code(): String {
        val output = "(${xTrainOutput.name}, ${yTrainOutput.name}), " +
            "(${xTestOutput.name}, ${yTestOutput.name})"
        return """
            |$output = ${datasetToCode.datasetToCode(dataset)}.load_data()
        """.trimMargin()
    }
}
