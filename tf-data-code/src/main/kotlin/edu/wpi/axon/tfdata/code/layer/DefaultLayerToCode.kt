package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.left
import arrow.core.right
import edu.wpi.axon.tfdata.code.asTuple
import edu.wpi.axon.tfdata.code.namedArguments
import edu.wpi.axon.tfdata.code.unquoted
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import org.koin.core.KoinComponent
import org.koin.core.inject

class DefaultLayerToCode : LayerToCode, KoinComponent {

    private val constraintToCode: ConstraintToCode by inject()
    private val initializerToCode: InitializerToCode by inject()
    private val regularizerToCode: RegularizerToCode by inject()

    override fun makeNewLayer(layer: Layer): Either<String, String> = when (layer) {
        is Layer.MetaLayer -> makeNewLayer(layer.layer)

        is Layer.InputLayer -> makeLayerCode(
            "tf.keras.Input",
            listOf(),
            listOf(
                "shape" to layer.batchInputShape.asTuple(),
                "batch_size" to layer.batchSize,
                "dtype" to layer.dtype,
                "sparse" to layer.sparse
            )
        ).right()

        is Layer.BatchNormalization -> Either.fx {
            makeLayerCode(
                "tf.keras.layers.BatchNormalization",
                listOf(),
                listOf(
                    "axis" to layer.axis,
                    "momentum" to layer.momentum,
                    "epsilon" to layer.epsilon,
                    "center" to layer.center,
                    "scale" to layer.scale,
                    "beta_initializer" to initializerToCode.makeNewInitializer(layer.betaInitializer).bind().unquoted(),
                    "gamma_initializer" to initializerToCode.makeNewInitializer(layer.gammaInitializer).bind().unquoted(),
                    "moving_mean_initializer" to initializerToCode.makeNewInitializer(layer.movingMeanInitializer).bind().unquoted(),
                    "moving_variance_initializer" to initializerToCode.makeNewInitializer(layer.movingVarianceInitializer).bind().unquoted(),
                    "beta_regularizer" to layer.betaRegularizer?.let {
                        regularizerToCode.makeNewRegularizer(
                            it
                        ).bind().unquoted()
                    },
                    "gamma_regularizer" to layer.gammaRegularizer?.let {
                        regularizerToCode.makeNewRegularizer(
                            it
                        ).bind().unquoted()
                    },
                    "beta_constraint" to layer.betaConstraint?.let {
                        constraintToCode.makeNewConstraint(
                            it
                        ).bind().unquoted()
                    },
                    "gamma_constraint" to layer.gammaConstraint?.let {
                        constraintToCode.makeNewConstraint(
                            it
                        ).bind().unquoted()
                    },
                    "renorm" to layer.renorm,
                    "renorm_clipping" to layer.renormClipping,
                    "renorm_momentum" to layer.renormMomentum,
                    "fused" to layer.fused,
                    "virtual_batch_size" to layer.virtualBatchSize,
                    "adjustment" to "None".unquoted(),
                    "name" to layer.name
                )
            )
        }

        is Layer.Dense -> makeLayerCode(
            "tf.keras.layers.Dense",
            listOf(),
            listOf(
                "units" to layer.units,
                "activation" to makeNewActivation(layer.activation).unquoted(),
                "name" to layer.name
            )
        ).right()

        is Layer.Dropout -> makeLayerCode(
            "tf.keras.layers.Dropout",
            listOf(layer.rate.toString()),
            listOf(
                "noise_shape" to layer.noiseShape?.asTuple(),
                "seed" to layer.seed,
                "name" to layer.name
            )
        ).right()

        is Layer.Flatten -> makeLayerCode(
            "tf.keras.layers.Flatten",
            listOf(),
            listOf(
                "data_format" to layer.dataFormat?.value,
                "name" to layer.name
            )
        ).right()

        is Layer.MaxPooling2D -> makeLayerCode(
            "tf.keras.layers.MaxPooling2D",
            listOf(),
            listOf(
                "pool_size" to layer.poolSize,
                "strides" to layer.strides,
                "padding" to layer.padding.value,
                "data_format" to layer.dataFormat?.value,
                "name" to layer.name
            )
        ).right()

        // TODO: Remove this
        else -> "Cannot construct an unknown layer: $layer".left()
    }

    override fun makeNewActivation(activation: Activation) = "tf.keras.activations." +
        when (activation) {
            is Activation.Linear -> "linear"
            is Activation.ReLu -> "relu"
            is Activation.SoftMax -> "softmax"
            is Activation.UnknownActivation -> throw IllegalArgumentException(
                "Cannot construct an unknown activation function: $activation"
            )
        }

    /**
     * Assembles the code to make a new layer.
     *
     * @param className The full class name of the layer.
     * @param args The unnamed arguments to init.
     * @param namedArgs The named arguments to init.
     * @return The code to make a new instance of the layer.
     */
    private fun makeLayerCode(
        className: String,
        args: List<String?>,
        namedArgs: List<Pair<String, *>>
    ): String {
        val argsString = args.joinToString(separator = ", ") { it ?: "None" }
        val optionalSeparator = if (args.isNotEmpty()) ", " else ""
        return "$className($argsString$optionalSeparator${namedArguments(namedArgs)})"
    }
}
