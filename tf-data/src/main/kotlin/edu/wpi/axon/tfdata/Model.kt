package edu.wpi.axon.tfdata

import edu.wpi.axon.tfdata.layer.SealedLayer

sealed class Model(
    open val name: String,
    open val batchInputShape: List<Int?>,
    open val layers: Set<SealedLayer.MetaLayer>
) {

    /**
     * A linear stack of layers.
     */
    data class Sequential(
        override val name: String,
        override val batchInputShape: List<Int?>,
        override val layers: Set<SealedLayer.MetaLayer>
    ) : Model(name, batchInputShape, layers)

    /**
     * An unknown model type.
     */
    data class Unknown(
        override val name: String,
        override val batchInputShape: List<Int?>,
        override val layers: Set<SealedLayer.MetaLayer>
    ) : Model(name, batchInputShape, layers)
}
