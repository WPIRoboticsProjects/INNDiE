package edu.wpi.axon.tfdata.layer

/**
 * A sealed [Layer] implementation.
 */
sealed class SealedLayer : Layer {

    /**
     * A sealed [Layer] which delegates to another [layer].
     */
    sealed class MetaLayer(open val layer: Layer) : SealedLayer() {

        /**
         * A [Layer] which is trainable.
         */
        data class TrainableLayer(
            override val name: String,
            override val layer: Layer
        ) : MetaLayer(layer), Layer by layer

        /**
         * A [Layer] which is untrainable.
         */
        data class UntrainableLayer(
            override val name: String,
            override val layer: Layer
        ) : MetaLayer(layer), Layer by layer
    }

    /**
     * A placeholder layer for a layer that Axon does not understand.
     */
    data class UnknownLayer(
        override val name: String
    ) : SealedLayer()

    /**
     * A Dense layer.
     *
     * @param units The number of neurons.
     * @param activation The [Activation] function.
     */
    data class Dense(
        override val name: String,
        val units: Int,
        val activation: Activation
    ) : SealedLayer()
}

/**
 * Creates a new [SealedLayer.MetaLayer.TrainableLayer].
 *
 * @receiver The [Layer] to wrap.
 */
fun Layer.trainable() = SealedLayer.MetaLayer.TrainableLayer(name, this)

/**
 * Creates a new [SealedLayer.MetaLayer.UntrainableLayer].
 *
 * @receiver The [Layer] to wrap.
 */
fun Layer.untrainable() =
    SealedLayer.MetaLayer.UntrainableLayer(name, this)
