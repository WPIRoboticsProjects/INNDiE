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

    var xTrain: Variable by singleAssign()
    var yTrain: Variable by singleAssign()
    var xTest: Variable by singleAssign()
    var yTest: Variable by singleAssign()

    private val datasetToCode: DatasetToCode by inject()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf")
    )

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable>
        get() = setOf(xTrain, yTrain, xTest, yTest)

    override val dependencies: Set<Code<*>>
        get() = setOf()

    override fun code(): String {
        val output = """(${xTrain.name}, ${yTrain.name}), (${xTest.name}, ${yTest.name})"""
        return """
            |$output = ${datasetToCode.datasetToCode(dataset)}.load_data()
        """.trimMargin()
    }
}
