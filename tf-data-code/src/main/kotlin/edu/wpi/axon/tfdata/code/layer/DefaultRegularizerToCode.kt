package edu.wpi.axon.tfdata.code.layer

import arrow.core.left
import arrow.core.right
import edu.wpi.axon.tfdata.layer.Regularizer

class DefaultRegularizerToCode : RegularizerToCode {

    override fun makeNewRegularizer(regularizer: Regularizer?) = when (regularizer) {
        is Regularizer.L1L2 ->
            "tf.keras.regularizers.L1L2(${regularizer.l1}, ${regularizer.l2})".right()

        else -> "Cannot make an unknown regularizer: $regularizer".left()
    }
}
