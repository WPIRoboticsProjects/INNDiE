package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.code.layer.LayerToCode
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.inndie.util.singleAssign
import org.koin.core.inject

/**
 * Adds and removes layers on a new model using a starting Sequential model.
 */
class ApplySequentialLayerDeltaTask(name: String) : BaseTask(name) {

    /**
     * The model to take the starting layers from.
     */
    var modelInput: Variable by singleAssign()

    /**
     * The current layers in the model.
     */
    var oldLayers: Set<Layer.MetaLayer> by singleAssign()

    /**
     * The layers the new model should have.
     */
    var newLayers: Set<Layer.MetaLayer> by singleAssign()

    /**
     * The variable to output the new model to.
     */
    var newModelOutput: Variable by singleAssign()

    private val layerToCode: LayerToCode by inject()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf")
    )

    override val inputs: Set<Variable>
        get() = setOf(modelInput)

    override val outputs: Set<Variable>
        get() = setOf(newModelOutput)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun isConfiguredCorrectly(): Boolean {
        return super.isConfiguredCorrectly() && oldLayers.all { it.inputs == null } &&
            newLayers.all { it.inputs == null }
    }

    override fun code(): String {
        val layerOperations = createLayerOperations(oldLayers, newLayers)
        val sequentialArgs = buildSequentialArgs(
            layerOperations = layerOperations,
            indent = 4
        )

        return """
        |${newModelOutput.name} = tf.keras.Sequential($sequentialArgs)
        |${buildTrainableFlags(layerOperations.map { it.layer }, newModelOutput)}
        """.trimMargin()
    }

    @Suppress("SameParameterValue")
    private fun buildSequentialArgs(layerOperations: List<LayerOperation>, indent: Int): String {
        val indentSpacing = " ".repeat(indent)
        val prefix = if (layerOperations.size > 1) "[\n$indentSpacing" else "["
        val postfix = if (layerOperations.size > 1) "\n]" else "]"

        return layerOperations.joinToString(
            prefix = prefix,
            separator = ",\n$indentSpacing",
            postfix = postfix
        ) {
            when (it) {
                is LayerOperation.CopyLayer -> getLayerInModel(modelInput, it.layer.name)
                is LayerOperation.MakeNewLayer -> layerToCode.makeNewLayer(it.layer).fold(
                    { throw IllegalStateException("Tried to make a new layer but got $it") },
                    { it }
                )
            }
        }
    }
}
