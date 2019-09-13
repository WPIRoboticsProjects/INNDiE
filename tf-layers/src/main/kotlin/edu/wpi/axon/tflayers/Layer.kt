package edu.wpi.axon.tflayers

interface Layer {

    val name: String
}

sealed class SealedLayer : Layer {

    sealed class MetaLayer(open val layer: Layer) : SealedLayer() {

        data class TrainableLayer(
            override val name: String,
            override val layer: Layer
        ) : MetaLayer(layer), Layer by layer

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

fun Layer.trainable() = SealedLayer.MetaLayer.TrainableLayer(name, this)

fun Layer.untrainable() = SealedLayer.MetaLayer.UntrainableLayer(name, this)
