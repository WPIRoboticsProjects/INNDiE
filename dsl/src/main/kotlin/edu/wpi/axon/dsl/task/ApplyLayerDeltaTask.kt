package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tflayers.Activation
import edu.wpi.axon.tflayers.Layer
import edu.wpi.axon.util.singleAssign

private sealed class LayerOperation {
    data class CopyLayer(val layer: Layer) : LayerOperation()
    data class MakeNewLayer(val layer: Layer) : LayerOperation()
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
    var currentLayers: List<Layer> by singleAssign()

    /**
     * The layers the new model should have.
     */
    var newLayers: List<Layer> by singleAssign()

    /**
     * The variable to output the new model to.
     */
    var newModelOutput: Variable by singleAssign()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf")
    )

    override val inputs: Set<Variable>
        get() = setOf(modelInput)

    override val outputs: Set<Variable>
        get() = setOf(newModelOutput)

    override val dependencies: Set<Code<*>> = setOf()

    override fun code(): String {
        val layerOperations = newLayers.map {
            if (it in currentLayers) {
                // Copy layers that are already in the base model to preserve as much configuration
                // information as possible
                LayerOperation.CopyLayer(it)
            } else {
                // We are forced to make new layers if they aren't in the base model
                LayerOperation.MakeNewLayer(it)
            }
        }

        return """
        |${newModelOutput.name} = tf.keras.Sequential(${buildOperations(layerOperations, 4)})
        """.trimMargin()
    }

    @Suppress("SameParameterValue")
    private fun buildOperations(layerOperations: List<LayerOperation>, indent: Int): String {
        val indentSpacing = " ".repeat(indent)
        val prefix = if (layerOperations.size > 1) "[\n$indentSpacing" else "["
        val postfix = if (layerOperations.size > 1) "\n]" else "]"

        return layerOperations.joinToString(
            prefix = prefix,
            separator = ",\n$indentSpacing",
            postfix = postfix
        ) {
            when (it) {
                is LayerOperation.CopyLayer -> """${modelInput.name}.get_layer("${it.layer.name}")"""
                is LayerOperation.MakeNewLayer -> layerConstructor(it.layer)
            }
        }
    }

    private fun layerConstructor(layer: Layer) = when (layer) {
        is Layer.Dense -> """tf.keras.layers.Dense(name="${layer.name}", """ +
            "trainable=${boolToPythonString(layer.trainable)}, " +
            "units=${layer.units}, " +
            "activation=${activationConstructor(layer.activation)})"

        is Layer.UnknownLayer ->
            throw IllegalArgumentException("Cannot construct an unknown layer: $layer")
    }

    private fun activationConstructor(activation: Activation) = "tf.keras.activations." +
        when (activation) {
            is Activation.ReLu -> "relu"
            is Activation.SoftMax -> "softmax"
            is Activation.UnknownActivation -> throw IllegalArgumentException(
                "Cannot construct an unknown activation function: $activation"
            )
        }

    private fun boolToPythonString(bool: Boolean): String = if (bool) "True" else "False"
}
