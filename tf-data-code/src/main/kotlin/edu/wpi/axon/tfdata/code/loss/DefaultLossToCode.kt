package edu.wpi.axon.tfdata.code.loss

import edu.wpi.axon.tfdata.loss.Loss

class DefaultLossToCode : LossToCode {

    override fun makeNewLoss(loss: Loss) = when (loss) {
        is Loss.SparseCategoricalCrossentropy -> "tf.keras.losses.sparse_categorical_crossentropy"
    }
}
