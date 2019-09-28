package edu.wpi.axon.dsl.task

import edu.wpi.axon.tfdata.layer.SealedLayer

internal sealed class LayerOperation(open val layer: SealedLayer.MetaLayer) {

    /**
     * Copy the layer from the old model to the new model.
     */
    data class CopyLayer(override val layer: SealedLayer.MetaLayer) : LayerOperation(layer)

    /**
     * Create a new layer in the new model.
     */
    data class MakeNewLayer(override val layer: SealedLayer.MetaLayer) : LayerOperation(layer)
}
