package edu.wpi.axon.dsl.task

import arrow.core.Either
import arrow.core.Some
import arrow.core.extensions.either.monadError.monadError
import arrow.core.extensions.fx
import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.UniqueVariableNameGenerator
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.code.layer.LayerToCode
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tflayerloader.layerGraphIsValid
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject

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
    var currentModel: Model.General by singleAssign()

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
                    layerGraphIsValid(currentModel.layers),
                    layerGraphIsValid(newModel.layers)
                )
            }
        }.fold({ false }, { true })
    }

    override fun code(): String {
        val handledLayers = mutableSetOf<SealedLayer.MetaLayer>()
        val layerVariableNames = mutableMapOf<SealedLayer.MetaLayer, String>()

        fun StringBuilder.appendLayerCode(layer: SealedLayer.MetaLayer) {
            if (layer !in handledLayers) {
                newModel.layers
                    .predecessors(layer)
                    .sortedBy { it.name }
                    .forEach {
                        appendLayerCode(it)
                    }

                val layerOp = when (layer.layer) {
                    // Always make a new input layer
                    is SealedLayer.InputLayer -> LayerOperation.MakeNewLayer(layer)

                    else -> if (layer in currentModel.layers.nodes()) {
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
            val correspondingLayer = newModel.layers.nodes().filter {
                it.layer is SealedLayer.InputLayer && it.name == input.id
            }.let {
                require(it.size == 1) {
                    "Found more than one matching InputLayer with id ${input.id}: " +
                        it.joinToString("\n")
                }
                it.first()
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
        layerVariableNames: Map<SealedLayer.MetaLayer, String>
    ) = when (val layer = layerOp.layer.layer) {
        is SealedLayer.InputLayer -> makeNewLayer(layer)

        else -> {
            val newLayerCode = when (layerOp) {
                is LayerOperation.CopyLayer -> getLayerInModel(modelInput, layer.name)
                is LayerOperation.MakeNewLayer -> makeNewLayer(layer)
            }

            val layerInputs = (layer.inputs as Some).t
            val layerInputCode = makeLayerInputCode(layerInputs, layerVariableNames)

            "$newLayerCode($layerInputCode)"
        }
    }

    private fun findInputIndex(
        newLayers: Set<Model.General.InputData>,
        layer: SealedLayer.InputLayer
    ) = newLayers.map { it.id }.indexOf(layer.name)

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
        layerVariableNames: Map<SealedLayer.MetaLayer, String>
    ): String {
        val entries = layerVariableNames.entries

        // Find the variable names corresponding to the layer names
        val variableNames = layerInputs.map { inputName ->
            entries.filter { it.key.name == inputName }
                .also {
                    require(it.size == 1) {
                        "Expected one matching variable name, got `${it.joinToString()}`. " +
                            "Check that the layer names are all unique."
                    }
                }
                .first()
                .value
        }

        return if (variableNames.size > 1) {
            variableNames.joinToString(prefix = "[", postfix = "]")
        } else {
            require(variableNames.isNotEmpty()) {
                "No variable names were found."
            }

            variableNames.first()
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
}
