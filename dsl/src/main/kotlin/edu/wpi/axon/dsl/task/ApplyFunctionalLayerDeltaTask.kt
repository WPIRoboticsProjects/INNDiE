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
        val layerOperations = createLayerOperations(
            currentModel.layers.nodes(),
            newModel.layers.nodes()
        )

        // Make a variable name for each layer. All of these need to be known before generating
        // the code.
        val layerVariableNames = layerOperations.map {
            it to variableNameGenerator.uniqueVariableName()
        }.toMap()

        val layerCode = layerVariableNames.map { (layerOp, variableName) ->
            "$variableName = " + makeLayerCode(layerOp, layerVariableNames)
        }.joinToString("\n")

        // We have to specify the
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

            layerVariableNames.entries.first { it.key.layer == correspondingLayer }.value
        }

        val modelCode = "${newModelOutput.name} = tf.keras.Model(inputs=$modelInputCode, " +
            "outputs=${layerVariableNames.values.last()})"

        val trainableFlagsCode = buildTrainableFlags(layerOperations, newModelOutput)

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
        layerVariableNames: Map<LayerOperation, String>
    ) = when (val layer = layerOp.layer.layer) {
        is SealedLayer.InputLayer -> when (layerOp) {
            // Copying an input layer needs this special syntax (because we need the Tensor
            // and not the layer itself). Also, get the index from the currentModel rather than the
            // new model because it corresponds with the model input.
            is LayerOperation.CopyLayer ->
                "${modelInput.name}.inputs[${findInputIndex(currentModel.layers.nodes(), layer)}]"

            is LayerOperation.MakeNewLayer -> makeNewLayer(layer)
        }

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
        newLayers: Set<SealedLayer.MetaLayer>,
        layer: SealedLayer.InputLayer
    ) = newLayers.map { it.layer }.indexOf(layer)

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
            entries.filter { it.key.layer.name == inputName }
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
