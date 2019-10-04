package edu.wpi.axon.tfdata.layer

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Right
import arrow.core.Tuple2
import edu.wpi.axon.tfdata.Model

/**
 * A sealed [Layer] implementation.
 */
sealed class SealedLayer : Layer {

    /**
     * Adds some information and delegates to another [SealedLayer].
     */
    sealed class MetaLayer(open val layer: SealedLayer) : SealedLayer() {

        /**
         * A layer which is trainable.
         *
         * @param trainable Whether this layer should be trained.
         */
        data class TrainableLayer(
            override val name: String,
            override val layer: SealedLayer,
            val trainable: Boolean
        ) : MetaLayer(layer), Layer by layer {
            init {
                require(layer !is MetaLayer)
            }
        }

        /**
         * A layer which is untrainable. This should not be confused with a [TrainableLayer]
         * where [TrainableLayer.trainable] is `true`. An [UntrainableLayer] is IMPOSSIBLE to train.
         */
        data class UntrainableLayer(
            override val name: String,
            override val layer: SealedLayer
        ) : MetaLayer(layer), Layer by layer {
            init {
                require(layer !is MetaLayer)
            }
        }
    }

    /**
     * A placeholder layer for a layer that Axon does not understand.
     */
    data class UnknownLayer(
        override val name: String,
        override val inputs: Option<Set<String>>
    ) : SealedLayer()

    /**
     * A layer that accepts input data and has no parameters.
     *
     * // TODO: tensor parameter
     */
    data class InputLayer
    private constructor(
        override val name: String,
        val batchInputShape: List<Int?>,
        val batchSize: Int? = null,
        val dtype: Number? = null,
        val sparse: Boolean = false
    ) : SealedLayer() {

        override val inputs: Option<Set<String>> = None

        fun toInputData(): Model.General.InputData =
            Model.General.InputData(name, batchInputShape, batchSize, dtype, sparse)

        companion object {
            operator fun invoke(
                name: String,
                shape: List<Int?>,
                batchSize: Int? = null,
                dtype: Number? = null,
                sparse: Boolean = false
            ) = InputLayer(name, shape, batchSize, dtype, sparse).untrainable()
        }
    }

    /**
     * A 2D convolutional layer.
     *
     * @param filters The output dimension.
     * @param kernel The (x,y) size of the kernel.
     * @param activation The [Activation] function.
     */
    data class Conv2D(
        override val name: String,
        override val inputs: Option<Set<String>>,
        val filters: Int,
        val kernel: Tuple2<Int, Int>,
        val activation: Activation
    ) : SealedLayer()

    /**
     * A Dense layer.
     *
     * @param units The number of neurons.
     * @param activation The [Activation] function.
     */
    data class Dense(
        override val name: String,
        override val inputs: Option<Set<String>>,
        val units: Int,
        val activation: Activation
    ) : SealedLayer()

    /**
     * A Dropout layer. Applies dropout to the input, randomly setting a fraction of input units to
     * `0` at each update during training time to counteract overfitting.
     *
     * @param rate The fraction of the input units to drop. Between `0` and `1`.
     * @param noiseShape A 1D integer tensor representing the shape of the binary dropout mask that
     * will be multiplied with the input.
     * @param seed The random seed.
     */
    data class Dropout(
        override val name: String,
        override val inputs: Option<Set<String>>,
        val rate: Double,
        val noiseShape: List<Int>? = null,
        val seed: Int? = null
    ) : SealedLayer() {

        init {
            require(rate in 0.0..1.0) {
                "rate ($rate) was outside the allowed range of [0, 1]."
            }
        }
    }

    /**
     * A MaxPooling2D layer.
     */
    data class MaxPooling2D(
        override val name: String,
        override val inputs: Option<Set<String>>,
        val poolSize: Either<Int, Tuple2<Int, Int>> = Right(Tuple2(2, 2)),
        val strides: Either<Int, Tuple2<Int, Int>>? = null,
        val padding: PoolingPadding = PoolingPadding.Valid,
        val dataFormat: PoolingDataFormat? = null
    ) : SealedLayer()
}

/**
 * Creates a new [SealedLayer.MetaLayer.TrainableLayer].
 *
 * @receiver The [Layer] to wrap.
 * @param trainable Whether this layer should be trained.
 */
fun SealedLayer.trainable(trainable: Boolean = true) =
    SealedLayer.MetaLayer.TrainableLayer(name, this, trainable)

/**
 * Creates a new [SealedLayer.MetaLayer.UntrainableLayer].
 *
 * @receiver The [Layer] to wrap.
 */
fun SealedLayer.untrainable() =
    SealedLayer.MetaLayer.UntrainableLayer(name, this)
