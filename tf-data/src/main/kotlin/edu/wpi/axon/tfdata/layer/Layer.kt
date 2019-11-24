package edu.wpi.axon.tfdata.layer

import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.SerializableEitherITii
import edu.wpi.axon.tfdata.SerializableTuple2II
import kotlinx.serialization.Serializable

/**
 * A TensorFlow layer.
 *
 * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/layers
 */
@Serializable
sealed class Layer {

    /**
     * The unique name of this layer.
     */
    abstract val name: String

    /**
     * Any inputs to this layer. Should be [None] for Sequential models and [Some] for other
     * models. Each element is the [name] of another layer.
     */
    abstract val inputs: Set<String>?

    /**
     * @param trainable Whether this layer should be trained.
     * @return A new [MetaLayer.TrainableLayer] that wraps this layer.
     */
    fun trainable(trainable: Boolean = true) =
        MetaLayer.TrainableLayer(name, inputs, this, trainable)

    /**
     * @return A new [MetaLayer.UntrainableLayer] that wraps this layer.
     */
    fun untrainable() =
        MetaLayer.UntrainableLayer(name, inputs, this)

    /**
     * Adds some information and delegates to another [Layer].
     */
    @Serializable
    sealed class MetaLayer : Layer() {

        abstract val layer: Layer

        /**
         * A layer which is trainable.
         *
         * @param trainable Whether this layer should be trained.
         */
        @Serializable
        data class TrainableLayer(
            override val name: String,
            override val inputs: Set<String>?,
            override val layer: Layer,
            val trainable: Boolean
        ) : MetaLayer() {
            init {
                require(layer !is MetaLayer)
            }
        }

        /**
         * A layer which is untrainable. This should not be confused with a [TrainableLayer]
         * where [TrainableLayer.trainable] is `true`. An [UntrainableLayer] is IMPOSSIBLE to train.
         */
        @Serializable
        data class UntrainableLayer(
            override val name: String,
            override val inputs: Set<String>?,
            override val layer: Layer
        ) : MetaLayer() {
            init {
                require(layer !is MetaLayer)
            }
        }
    }

    /**
     * A placeholder layer for a layer that Axon does not understand.
     */
    @Serializable
    data class UnknownLayer(
        override val name: String,
        override val inputs: Set<String>?
    ) : Layer()

    /**
     * A layer that contains an entire model inside it.
     *
     * @param model The model that acts as this layer.
     */
    @Serializable
    data class ModelLayer(
        override val name: String,
        override val inputs: Set<String>?,
        val model: Model
    ) : Layer()

    /**
     * A layer that accepts input data and has no parameters.
     *
     * // TODO: tensor parameter
     */
    @Serializable
    data class InputLayer
    private constructor(
        override val name: String,
        val batchInputShape: List<Int?>,
        val batchSize: Int? = null,
        val dtype: Double? = null,
        val sparse: Boolean = false
    ) : Layer() {

        override val inputs: Set<String>? = null

        fun toInputData(): Model.General.InputData =
            Model.General.InputData(name, batchInputShape, batchSize, dtype, sparse)

        companion object {
            operator fun invoke(
                name: String,
                shape: List<Int?>,
                batchSize: Int? = null,
                dtype: Double? = null,
                sparse: Boolean = false
            ) = InputLayer(name, shape, batchSize, dtype, sparse).untrainable()
        }
    }

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/layers/BatchNormalization
     *
     * Does not support the `adjustment` parameter.
     */
    @Serializable
    data class BatchNormalization(
        override val name: String,
        override val inputs: Set<String>?,
        val axis: Int = -1,
        val momentum: Double = 0.99,
        val epsilon: Double = 0.001,
        val center: Boolean = true,
        val scale: Boolean = true,
        val betaInitializer: Initializer = Initializer.Zeros,
        val gammaInitializer: Initializer = Initializer.Ones,
        val movingMeanInitializer: Initializer = Initializer.Zeros,
        val movingVarianceInitializer: Initializer = Initializer.Ones,
        val betaRegularizer: Regularizer? = null,
        val gammaRegularizer: Regularizer? = null,
        val betaConstraint: Constraint? = null,
        val gammaConstraint: Constraint? = null,
        val renorm: Boolean = false,
        val renormClipping: Map<String, Double>? = null,
        val renormMomentum: Double? = 0.99,
        val fused: Boolean? = null,
        val virtualBatchSize: Int? = null
    ) : Layer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/layers/AveragePooling2D
     */
    @Serializable
    data class AveragePooling2D(
        override val name: String,
        override val inputs: Set<String>?,
        val poolSize: SerializableEitherITii =
            SerializableEitherITii.Right(SerializableTuple2II(2, 2)),
        val strides: SerializableEitherITii? = null,
        val padding: PoolingPadding = PoolingPadding.Valid,
        val dataFormat: DataFormat? = null
    ) : Layer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/layers/Conv2D
     */
    @Serializable
    data class Conv2D(
        override val name: String,
        override val inputs: Set<String>?,
        val filters: Int,
        val kernel: SerializableTuple2II,
        val activation: Activation
    ) : Layer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/layers/Dense
     */
    @Serializable
    data class Dense(
        override val name: String,
        override val inputs: Set<String>?,
        val units: Int,
        val activation: Activation = Activation.Linear,
        val useBias: Boolean = true,
        val kernelInitializer: Initializer = Initializer.GlorotUniform(null),
        val biasInitializer: Initializer = Initializer.Zeros,
        val kernelRegularizer: Regularizer? = null,
        val biasRegularizer: Regularizer? = null,
        val activityRegularizer: Regularizer? = null,
        val kernelConstraint: Constraint? = null,
        val biasConstraint: Constraint? = null
    ) : Layer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/layers/Dropout
     */
    @Serializable
    data class Dropout(
        override val name: String,
        override val inputs: Set<String>?,
        val rate: Double,
        val noiseShape: List<Int>? = null,
        val seed: Int? = null
    ) : Layer() {

        init {
            require(rate in 0.0..1.0) {
                "rate ($rate) was outside the allowed range of [0, 1]."
            }
        }
    }

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/layers/Flatten
     */
    @Serializable
    data class Flatten(
        override val name: String,
        override val inputs: Set<String>?,
        val dataFormat: DataFormat? = null
    ) : Layer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/layers/GlobalAveragePooling2D
     */
    @Serializable
    data class GlobalAveragePooling2D(
        override val name: String,
        override val inputs: Set<String>?,
        val dataFormat: DataFormat?
    ) : Layer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/layers/GlobalMaxPool2D
     */
    @Serializable
    data class GlobalMaxPooling2D(
        override val name: String,
        override val inputs: Set<String>?,
        val dataFormat: DataFormat? = null
    ) : Layer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/layers/MaxPool2D
     */
    @Serializable
    data class MaxPooling2D(
        override val name: String,
        override val inputs: Set<String>?,
        val poolSize: SerializableEitherITii =
            SerializableEitherITii.Right(SerializableTuple2II(2, 2)),
        val strides: SerializableEitherITii? = null,
        val padding: PoolingPadding = PoolingPadding.Valid,
        val dataFormat: DataFormat? = null
    ) : Layer()

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/layers/SpatialDropout2D
     */
    @Serializable
    data class SpatialDropout2D(
        override val name: String,
        override val inputs: Set<String>?,
        val rate: Double,
        val dataFormat: DataFormat? = null
    ) : Layer() {

        init {
            require(rate in 0.0..1.0) {
                "rate ($rate) was outside the allowed range of [0, 1]."
            }
        }
    }

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/layers/UpSampling2D
     *
     * Bug: TF does not export a value for [interpolation].
     */
    @Serializable
    data class UpSampling2D(
        override val name: String,
        override val inputs: Set<String>?,
        val size: SerializableEitherITii =
            SerializableEitherITii.Right(SerializableTuple2II(2, 2)),
        val dataFormat: DataFormat? = null,
        val interpolation: Interpolation = Interpolation.Nearest
    ) : Layer()
}
