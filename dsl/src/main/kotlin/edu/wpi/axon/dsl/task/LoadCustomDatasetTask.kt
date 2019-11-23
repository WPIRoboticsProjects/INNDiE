package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.UniqueVariableNameGenerator
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.code.ExampleDatasetToCode
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject

/**
 * Loads a custom dataset.
 */
class LoadCustomDatasetTask(name: String) : BaseTask(name) {

    /**
     * The dataset to load.
     */
    var dataset: Dataset.Custom by singleAssign()

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

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf"),
        makeImport("import axon.client")
    )

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable>
        get() = setOf(xTrainOutput, yTrainOutput, xTestOutput, yTestOutput)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    private val variableNameGenerator: UniqueVariableNameGenerator by inject()

    override fun code(): String {
        val descriptionVar = variableNameGenerator.uniqueVariableName()
        return """
            |axon.client.convert_dataset(${dataset.pathInS3})
            |$descriptionVar = {
            |   ${TODO()}
            |}
        """.trimMargin()
    }
}
