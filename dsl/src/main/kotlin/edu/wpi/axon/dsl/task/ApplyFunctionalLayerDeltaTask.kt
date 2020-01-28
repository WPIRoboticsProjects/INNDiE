package edu.wpi.axon.dsl.task

import arrow.core.Either
import arrow.core.extensions.either.monadError.monadError
import arrow.core.extensions.fx
import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.UniqueVariableNameGenerator
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.code.layer.LayerToCode
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tflayerloader.layerGraphIsValid
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject
import org.octogonapus.ktguava.collections.toImmutableList

/**
 * Adds and removes layers on a new model using a starting general model (i.e. not a Sequential
 * model).
 */
@Suppress("UnstableApiUsage")
class ApplyFunctionalLayerDeltaTask(name: String) : BaseTask(name) {

    /**
     * The model to take the starting layers from.
     */
    var modelInput: Variable by singleAssign()

    /**
     * The current model.
     */
    var oldModel: Model.General by singleAssign()

    /**
     * The new model.
     */
    var newModel: Model.General by singleAssign()

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
        return super.isConfiguredCorrectly() && Either.fx<String, Unit> {
            !Either.monadError<String>().run {
                tupled(
                    layerGraphIsValid(oldModel.layers),
                    layerGraphIsValid(newModel.layers)
                )
            }
        }.fold({ false }, { true }) && inputsAreConsistent(oldModel) &&
            inputsAreConsistent(newModel)
    }

    private fun inputsAreConsistent(model: Model.General) = model.input == model.layers.nodes()
        .mapNotNullTo(mutableSetOf()) { (it.layer as? Layer.InputLayer)?.toInputData() }

    override fun code(): String {
        val handledLayers = mutableSetOf<Layer.MetaLayer>()
        val layerVariableNames = mutableMapOf<Layer.MetaLayer, String>()
        // The layers in the oldModel with the MetaLayer part stripped off. Used to compute the
        // layer ops
        val oldLayers = oldModel.layers.nodes().map { it.layer }.toImmutableList()

        fun StringBuilder.appendLayerCode(layer: Layer.MetaLayer) {
            if (layer !in handledLayers) {
                newModel.layers
                    // Append the predecessors first because they are the dependencies of this node
                    .predecessors(layer)
                    // Make the ordering consistent
                    .sortedBy { it.name }
                    .forEach {
                        appendLayerCode(it)
                    }

                val layerOp = when (layer.layer) {
                    // Always make a new input layer
                    is Layer.InputLayer -> LayerOperation.MakeNewLayer(layer)

                    else -> if (layer.layer in oldLayers) {
                        LayerOperation.CopyLayer(layer)
                    } else {
                        LayerOperation.MakeNewLayer(layer)
                    }
                }

                val varName = variableNameGenerator.uniqueVariableName()
                append("$varName = ${makeLayerCode(layerOp, layerVariableNames)}")
                append('\n')
                layerVariableNames[layer] = varName
                handledLayers += layer
            }
        }

        val layerCode = buildString {
            newModel.output.forEach { output ->
                appendLayerCode(newModel.layers.nodes().first { it.name == output.id })
            }
        }.trim()

        // We have to specify the inputs on the new model because the layers in the new model assume
        // the input format in the new model
        val modelInputCode = newModel.input.joinToString(prefix = "[", postfix = "]") { input ->
            // There will only be one matching layer because all the layer names are unique
            val correspondingLayer = newModel.layers.nodes().first {
                it.layer is Layer.InputLayer && it.name == input.id
            }

            layerVariableNames.entries.first { it.key == correspondingLayer }.value
        }

        val modelOutputCode = newModel.output.joinToString(prefix = "[", postfix = "]") { output ->
            layerVariableNames[newModel.layers.nodes().first { it.name == output.id }]!!
        }

        val modelCode =
            "${newModelOutput.name} = tf.keras.Model(inputs=$modelInputCode, outputs=$modelOutputCode)"

        val trainableFlagsCode = buildTrainableFlags(newModel.layers.nodes(), newModelOutput)

        return layerCode + "\n" + modelCode + "\n" + trainableFlagsCode
    }

    /**
     * Generates the code to either copy or make a new layer and compute the forward pass.
     *
     * @param layerOp The layer operation to perform.
     * @param layerVariableNames The variable names for all the layer operations.
     * @return Code for this [layerOp].
     */
    private fun makeLayerCode(
        layerOp: LayerOperation,
        layerVariableNames: Map<Layer.MetaLayer, String>
    ) = when (val layer = layerOp.layer.layer) {
        is Layer.InputLayer -> {
            // The first element should be null (should be null in the HDF5 file) because it's the
            // "length" of the data. The other elements are the real shape. We need to drop it
            // before generating the input layer, though, because the input layer requires the shape
            // of one data element.
            check(layer.batchInputShape.first() == null)
            makeNewLayer(layer.copy(batchInputShape = layer.batchInputShape.drop(1)))
        }

        else -> {
            val newLayerCode = when (layerOp) {
                is LayerOperation.CopyLayer -> getLayerInModel(modelInput, layer.name)
                is LayerOperation.MakeNewLayer -> makeNewLayer(layer)
            }

            val layerInputCode = makeLayerInputCode(layer.inputs!!, layerVariableNames)

            "$newLayerCode($layerInputCode)"
        }
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
        layerVariableNames: Map<Layer.MetaLayer, String>
    ): String {
        val entries = layerVariableNames.entries

        // Find the variable names corresponding to the layer names
        val variableNames = layerInputs.map { inputName ->
            // There will only be one matching layer because all the layer names are unique
            entries.first { it.key.name == inputName }.value
        }

        return if (variableNames.size > 1) {
            variableNames.joinToString(prefix = "[", postfix = "]")
        } else {
            // There will be at least one because all layer's inputs must be declared previously
            // in the graph
            variableNames.first()
        }
    }

    /**
     * Generates the code to make a new layer or throws an exception if it failed.
     *
     * @param layer The layer.
     * @return The code for the layer.
     */
    private fun makeNewLayer(layer: Layer) =
        layerToCode.makeNewLayer(layer).fold(
            { throw IllegalStateException("Tried to make a new layer but got $it") },
            { it }
        )
}
