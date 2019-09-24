package edu.wpi.axon.dsl.task

import arrow.core.Some
import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.UniqueVariableNameGenerator
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.code.layer.LayerToCode
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.util.allIn
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject

/**
 * Adds and removes layers on a new model using a starting general model (i.e. not a Sequential
 * model).
 */
class ApplyFunctionalLayerDeltaTask(name: String) : BaseTask(name) {

    /**
     * The model to take the starting layers from.
     */
    var modelInput: Variable by singleAssign()

    /**
     * The current layers in the model.
     */
    var currentLayers: Set<SealedLayer.MetaLayer> by singleAssign()

    /**
     * The layers the new model should have.
     */
    var newLayers: Set<SealedLayer.MetaLayer> by singleAssign()

    /**
     * The variable to output the new model to.
     */
    var newModelOutput: Variable by singleAssign()

    private val layerToCode: LayerToCode by inject()

    private val variableNameGenerator: UniqueVariableNameGenerator by inject()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf")
    )

    override val inputs: Set<Variable>
        get() = setOf(modelInput)

    override val outputs: Set<Variable>
        get() = setOf(newModelOutput)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun isConfiguredCorrectly(): Boolean {
        return super.isConfiguredCorrectly() &&
            // All layers must have inputs
            currentLayers.all { hasInputs(it) } && newLayers.all { hasInputs(it) } &&
            // The first layer in each set must be an InputLayer
            firstLayerIsInputLayer(currentLayers) && firstLayerIsInputLayer(newLayers) &&
            newLayers.fold(true) { acc, elem ->
                // Get the part of the list until the current element
                val prevList = newLayers.takeWhile { it != elem }
                val prevNames = prevList.map { it.name }

                // All of the inputs must be declared already, so they need to appear higher in the
                // list of layers
                acc && elem.inputs.fold({ emptySet<String>() }, { it }) allIn prevNames
            }
    }

    /**
     * A layer must have inputs (unless it's a [SealedLayer.InputLayer], which can't have inputs).
     *
     * @param layer The layer.
     * @return Whether the layer has inputs.
     */
    private fun hasInputs(layer: SealedLayer.MetaLayer) =
        layer.inputs is Some || layer.layer is SealedLayer.InputLayer

    /**
     * @param layers The set of layers.
     * @return Whether the first layer in the set is a [SealedLayer.InputLayer].
     */
    private fun firstLayerIsInputLayer(layers: Set<SealedLayer.MetaLayer>) =
        layers.firstOrNull()?.let { it.layer is SealedLayer.InputLayer } ?: true

    override fun code(): String {
        val layerOperations = createLayerOperations(currentLayers, newLayers)

        // Make a variable name for each layer. All of these need to be known before generating
        // the code.
        val layerVariableNames = layerOperations.map {
            it to variableNameGenerator.uniqueVariableName()
        }.toMap()

        val layerCode = layerVariableNames.map { (layerOp, name) ->
            "$name = " + when (val layer = layerOp.layer.layer) {
                is SealedLayer.InputLayer -> when (layerOp) {
                    // Copying an input layer needs this special syntax (because we need the Tensor
                    // and not the layer itself).
                    is LayerOperation.CopyLayer -> "${modelInput.name}.input"
                    is LayerOperation.MakeNewLayer -> makeNewLayer(layer)
                }

                else -> {
                    val newLayerCode = when (layerOp) {
                        is LayerOperation.CopyLayer -> getLayerInModel(modelInput.name, layer.name)
                        is LayerOperation.MakeNewLayer -> makeNewLayer(layer)
                    }

                    val layerInputs = (layer.inputs as Some).t
                    val layerInputCode = makeLayerInputCode(layerInputs, layerVariableNames)

                    "$newLayerCode($layerInputCode)"
                }
            }
        }.joinToString("\n")

        val modelCode = "${newModelOutput.name} = tf.keras.Model(inputs=" +
            "${layerVariableNames.values.first()}, outputs=${layerVariableNames.values.last()})"

        return "$layerCode\n$modelCode"
    }

    /**
     * Generates the code for the inputs to a layer (the code that the layer is "called" with
     * to make connections between layers).
     *
     * @param layerInputs The inputs to the layer (other layer names).
     * @param layerVariableNames The variable names for all the layers.
     * @return The layer input code.
     */
    private fun makeLayerInputCode(
        layerInputs: Set<String>,
        layerVariableNames: Map<LayerOperation, String>
    ): String {
        val entries = layerVariableNames.entries

        // Find the variable names corresponding to the layer names
        val variableNames = layerInputs.map { inputName ->
            // Corresponding layer
            entries.filter { it.key.layer.name == inputName }
                .map { it.value }
                .also { require(it.size == 1) }
                .first()
        }

        return if (variableNames.size > 1) {
            variableNames.joinToString(prefix = "[", postfix = "]")
        } else {
            variableNames.firstOrNull() ?: ""
        }
    }

    /**
     * Generates the code to make a new layer or throws an exception if it failed.
     *
     * @param layer The layer.
     * @return The code for the layer.
     */
    private fun makeNewLayer(layer: SealedLayer) =
        layerToCode.makeNewLayer(layer).fold(
            { throw IllegalStateException("Tried to make a new layer but got $it") },
            { it }
        )

    private fun getLayerInModel(modelName: String, layerName: String) =
        """$modelName.get_layer("$layerName")"""
}
