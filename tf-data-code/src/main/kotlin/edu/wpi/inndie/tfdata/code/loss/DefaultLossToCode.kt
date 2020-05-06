package edu.wpi.inndie.tfdata.code.loss

import edu.wpi.axon.tfdata.loss.Loss

class DefaultLossToCode : LossToCode {

    override fun makeNewLoss(loss: Loss) = when (loss) {
        is Loss.CategoricalCrossentropy -> "tf.keras.losses.categorical_crossentropy"
        is Loss.SparseCategoricalCrossentropy -> "tf.keras.losses.sparse_categorical_crossentropy"
        is Loss.MeanSquaredError -> "tf.keras.losses.mean_squared_error"
    }
}
