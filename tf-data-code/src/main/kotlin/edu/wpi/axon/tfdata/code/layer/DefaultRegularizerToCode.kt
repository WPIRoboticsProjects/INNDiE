package edu.wpi.axon.tfdata.code.layer

import arrow.core.left
import arrow.core.right
import edu.wpi.axon.tfdata.layer.Regularizer

class DefaultRegularizerToCode : RegularizerToCode {

    override fun makeNewRegularizer(regularizer: Regularizer?) = when (regularizer) {
        is Regularizer.L1 -> makeNewRegularizer("l1", listOf(regularizer.l)).right()

        is Regularizer.L2 -> makeNewRegularizer("l2", listOf(regularizer.l)).right()

        is Regularizer.L1L2 -> makeNewRegularizer(
            "L1L2",
            listOf(regularizer.l1, regularizer.l2)
        ).right()

        else -> "Cannot make an unknown regularizer: $regularizer".left()
    }

    private fun makeNewRegularizer(className: String, args: List<Number>): String {
        return "tf.keras.regularizers.$className(${args.joinToString()})"
    }
}
