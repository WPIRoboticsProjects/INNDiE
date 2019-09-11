package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tflayers.Activation
import edu.wpi.axon.tflayers.Layer
import edu.wpi.axon.util.singleAssign

/**
 * Adds and removes layers on a new model using a starting model.
 */
class ApplyLayerDeltaTask(name: String) : BaseTask(name) {

    /**
     * The model to take the starting layers from.
     */
    var modelInput: Variable by singleAssign()

    /**
     * The layers to add.
     */
    var layersToAdd: List<Layer> by singleAssign()

    /**
     * The layers to remove.
     */
    var layersToRemove: List<Layer> by singleAssign()

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
        val layersToAddName = uniqueVariableNameGenerator.uniqueVariableName()
        val layersToRemoveName = uniqueVariableNameGenerator.uniqueVariableName()
        return """
        |${newModelOutput.name} = tf.keras.Sequential()
        |$layersToAddName = ${layerConstructors(layersToAdd, layersToAddName.length + 4)}
        |$layersToRemoveName = ${layerNames(layersToRemove)}
        |
        |for layer in ${modelInput.name}.layers:
        |   if layer.name not in $layersToRemoveName:
        |       ${newModelOutput.name}.add(layer)
        """.trimMargin()
    }

    private fun layerNames(layers: List<Layer>) =
        layers.joinToString(prefix = "[", separator = ", ", postfix = "]") { """"${it.name}"""" }

    private fun layerConstructors(layers: List<Layer>, indent: Int) =
        layers.joinToString(prefix = "[", separator = ",\n${" ".repeat(indent)}", postfix = "]") {
            when (it) {
                is Layer.Dense -> """tf.keras.layers.Dense(name="${it.name}", """ +
                    "trainable=${boolToPythonString(it.trainable)}, " +
                    "units=${it.units}, " +
                    "activation=${activationConstructor(it.activation)})"

                is Layer.UnknownLayer ->
                    throw IllegalArgumentException("Cannot construct an unknown layer: $it")
            }
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
