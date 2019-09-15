package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.code.layer.LayerToCode
import edu.wpi.axon.tfdata.code.boolToPythonString
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject

private sealed class LayerOperation(open val layer: SealedLayer.MetaLayer) {
    data class CopyLayer(override val layer: SealedLayer.MetaLayer) : LayerOperation(layer)
    data class MakeNewLayer(override val layer: SealedLayer.MetaLayer) : LayerOperation(layer)
}

/**
 * Adds and removes layers on a new model using a starting model.
 */
class ApplyLayerDeltaTask(name: String) : BaseTask(name) {

    /**
     * The model to take the starting layers from.
     */
    var modelInput: Variable by singleAssign()

    /**
     * The current layers in the model.
     */
    var currentLayers: List<SealedLayer.MetaLayer> by singleAssign()

    /**
     * The layers the new model should have.
     */
    var newLayers: List<SealedLayer.MetaLayer> by singleAssign()

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

    override fun code(): String {
        val layerOperations = createLayerOperations(currentLayers)

        return """
        |${newModelOutput.name} = tf.keras.Sequential(${buildSequentialArgs(layerOperations, 4)})
        |${buildTrainableFlags(layerOperations)}
        """.trimMargin()
    }

    private fun createLayerOperations(currentLayers: List<SealedLayer.MetaLayer>): List<LayerOperation> {
        // The base layers inside the Trainable or Untrainable layer wrappers
        val innerCurrentLayers = currentLayers.map { it.layer }

        return newLayers.map {
            // Compare using the inner layer so the trainable status does not matter
            if (it.layer in innerCurrentLayers) {
                // Copy layers that are already in the base model to preserve as much
                // configuration information as possible
                LayerOperation.CopyLayer(it)
            } else {
                // We are forced to make new layers if they aren't in the base model
                LayerOperation.MakeNewLayer(it)
            }
        }
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
                is LayerOperation.CopyLayer -> getLayerInModel(modelInput.name, it.layer.name)
                is LayerOperation.MakeNewLayer -> layerToCode.makeNewLayer(it.layer)
            }
        }
    }

    private fun buildTrainableFlags(layerOperations: List<LayerOperation>): String =
        layerOperations.joinToString(separator = "\n") {
            val layerInModel = getLayerInModel(newModelOutput.name, it.layer.name)
            when (it.layer) {
                is SealedLayer.MetaLayer.TrainableLayer ->
                    """$layerInModel.trainable = ${boolToPythonString(true)}"""
                is SealedLayer.MetaLayer.UntrainableLayer ->
                    """$layerInModel.trainable = ${boolToPythonString(false)}"""
            }
        }

    private fun getLayerInModel(modelName: String, layerName: String) =
        """$modelName.get_layer("$layerName")"""
}
