package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.code.boolToPythonString
import edu.wpi.axon.tfdata.layer.SealedLayer

/**
 * Determines the layer operations to transform [currentLayers] into [newLayers].
 *
 * @param currentLayers The layers currently in the model.
 * @param newLayers The layers that will be in the new model.
 * @return The necessary layer operations.
 */
internal fun createLayerOperations(
    currentLayers: Iterable<SealedLayer.MetaLayer>,
    newLayers: Iterable<SealedLayer.MetaLayer>
): List<LayerOperation> {
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

/**
 * Generates the code that sets the `trainable` flag on each layer.
 *
 * @param layerOperations The layer operations that have already run.
 * @param model The model the layer operations were applied to.
 * @return The code to set the `trainable` flags.
 */
internal fun buildTrainableFlags(
    layerOperations: Iterable<SealedLayer.MetaLayer>,
    model: Variable
) = layerOperations.mapNotNull {
    when (it) {
        is SealedLayer.MetaLayer.TrainableLayer -> {
            val layerInModel = getLayerInModel(model, it.layer.name)
            """$layerInModel.trainable = ${boolToPythonString(it.trainable)}"""
        }

        is SealedLayer.MetaLayer.UntrainableLayer -> null
    }
}.joinToString("\n")

/**
 * @param model The model.
 * @param layerName The name of the layer.
 * @return The code to access a layer in the model by name.
 */
internal fun getLayerInModel(model: Variable, layerName: String) =
    """${model.name}.get_layer("$layerName")"""