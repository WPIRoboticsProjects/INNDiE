package edu.wpi.axon.dsl.task

import edu.wpi.inndie.tfdata.layer.Layer

internal sealed class LayerOperation(open val layer: Layer.MetaLayer) {

    /**
     * Copy the layer from the old model to the new model.
     */
    data class CopyLayer(override val layer: Layer.MetaLayer) : LayerOperation(layer)

    /**
     * Create a new layer in the new model.
     */
    data class MakeNewLayer(override val layer: Layer.MetaLayer) : LayerOperation(layer)
}
