package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.left
import arrow.core.right
import edu.wpi.axon.tfdata.code.boolToPythonString
import edu.wpi.axon.tfdata.code.listToPythonTuple
import edu.wpi.axon.tfdata.code.mapToPythonString
import edu.wpi.axon.tfdata.code.namedArguments
import edu.wpi.axon.tfdata.code.numberToPythonString
import edu.wpi.axon.tfdata.code.quoted
import edu.wpi.axon.tfdata.code.tupleToPythonTuple
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.layer.SealedLayer
import org.koin.core.KoinComponent
import org.koin.core.inject

class DefaultLayerToCode : LayerToCode, KoinComponent {

    private val constraintToCode: ConstraintToCode by inject()
    private val initializerToCode: InitializerToCode by inject()
    private val regularizerToCode: RegularizerToCode by inject()

    override fun makeNewLayer(layer: Layer): Either<String, String> = when (layer) {
        is SealedLayer.MetaLayer -> makeNewLayer(layer.layer)

        is SealedLayer.InputLayer -> makeLayerCode(
            "tf.keras.Input",
            listOf(),
            listOf(
                "shape" to listToPythonTuple(layer.batchInputShape, ::numberToPythonString),
                "batch_size" to numberToPythonString(layer.batchSize),
                "dtype" to numberToPythonString(layer.dtype),
                "sparse" to boolToPythonString(layer.sparse)
            )
        ).right()

        is SealedLayer.BatchNormalization -> Either.fx {
            makeLayerCode(
                "tf.keras.layers.BatchNormalization",
                listOf(),
                listOf(
                    "axis" to layer.axis.toString(),
                    "momentum" to layer.momentum.toString(),
                    "epsilon" to layer.epsilon.toString(),
                    "center" to boolToPythonString(layer.center),
                    "scale" to boolToPythonString(layer.scale),
                    "beta_initializer" to !initializerToCode.makeNewInitializer(layer.betaInitializer),
                    "gamma_initializer" to !initializerToCode.makeNewInitializer(layer.gammaInitializer),
                    "moving_mean_initializer" to !initializerToCode.makeNewInitializer(layer.movingMeanInitializer),
                    "moving_variance_initializer" to !initializerToCode.makeNewInitializer(layer.movingVarianceInitializer),
                    "beta_regularizer" to !regularizerToCode.makeNewRegularizer(layer.betaRegularizer),
                    "gamma_regularizer" to !regularizerToCode.makeNewRegularizer(layer.gammaRegularizer),
                    "beta_constraint" to !constraintToCode.makeNewConstraint(layer.betaConstraint),
                    "gamma_constraint" to !constraintToCode.makeNewConstraint(layer.gammaConstraint),
                    "renorm" to boolToPythonString(layer.renorm),
                    "renorm_clipping" to mapToPythonString(layer.renormClipping),
                    "renorm_momentum" to layer.renormMomentum.toString(),
                    "fused" to boolToPythonString(layer.fused),
                    "virtual_batch_size" to numberToPythonString(layer.virtualBatchSize),
                    "adjustment" to "None",
                    "name" to quoted(layer.name)
                )
            )
        }

        is SealedLayer.Dense -> makeLayerCode(
            "tf.keras.layers.Dense",
            listOf(),
            listOf(
                "units" to layer.units.toString(),
                "activation" to makeNewActivation(layer.activation),
                "name" to quoted(layer.name)
            )
        ).right()

        is SealedLayer.Dropout -> makeLayerCode(
            "tf.keras.layers.Dropout",
            listOf(layer.rate.toString()),
            listOf(
                "noise_shape" to listToPythonTuple(layer.noiseShape),
                "seed" to numberToPythonString(layer.seed),
                "name" to quoted(layer.name)
            )
        ).right()

        is SealedLayer.Flatten -> makeLayerCode(
            "tf.keras.layers.Flatten",
            listOf(),
            listOf(
                "data_format" to quoted(layer.dataFormat?.value),
                "name" to quoted(layer.name)
            )
        ).right()

        is SealedLayer.MaxPooling2D -> {
            val poolSizeString = when (val poolSize = layer.poolSize) {
                is Either.Left -> poolSize.a.toString()
                is Either.Right -> tupleToPythonTuple(poolSize.b)
            }

            val stridesString = when (val strides = layer.strides) {
                is Either.Left -> strides.a.toString()
                is Either.Right -> tupleToPythonTuple(strides.b)
                null -> "None"
            }

            makeLayerCode(
                "tf.keras.layers.MaxPooling2D",
                listOf(),
                listOf(
                    "pool_size" to poolSizeString,
                    "strides" to stridesString,
                    "padding" to quoted(layer.padding.value),
                    "data_format" to quoted(layer.dataFormat?.value),
                    "name" to quoted(layer.name)
                )
            ).right()
        }

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
        namedArgs: List<Pair<String, String?>>
    ): String {
        val argsString = args.joinToString(separator = ", ") { it ?: "None" }
        val optionalSeparator = if (args.isNotEmpty()) ", " else ""
        return "$className($argsString$optionalSeparator${namedArguments(namedArgs)})"
    }
}
