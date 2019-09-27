package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import edu.wpi.axon.tfdata.code.boolToPythonString
import edu.wpi.axon.tfdata.code.listToPythonTuple
import edu.wpi.axon.tfdata.code.numberToPythonString
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.layer.SealedLayer

class DefaultLayerToCode : LayerToCode {

    override fun makeNewLayer(layer: Layer): Either<String, String> = when (layer) {
        is SealedLayer.MetaLayer -> makeNewLayer(layer.layer)

        is SealedLayer.InputLayer -> ("tf.keras.Input(" +
            "shape=${listToPythonTuple(layer.batchInputShape, ::numberToPythonString)}, " +
            "batch_size=${numberToPythonString(layer.batchSize)}, " +
            "dtype=${numberToPythonString(layer.dtype)}, " +
            "sparse=${boolToPythonString(layer.sparse)})").right()

        is SealedLayer.Dense -> ("""tf.keras.layers.Dense(name="${layer.name}", """ +
            "units=${layer.units}, " +
            "activation=${makeNewActivation(layer.activation)})").right()

        else -> "Cannot construct an unknown layer: $layer".left()
    }

    override fun makeNewActivation(activation: Activation) = "tf.keras.activations." +
        when (activation) {
            is Activation.ReLu -> "relu"
            is Activation.SoftMax -> "softmax"
            is Activation.UnknownActivation -> throw IllegalArgumentException(
                "Cannot construct an unknown activation function: $activation"
            )
        }
}
