package edu.wpi.axon.tfdata.code.loss

import edu.wpi.axon.tfdata.loss.Loss
import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DefaultLossToCodeTest {

    private val lossToCode = DefaultLossToCode()

    @ParameterizedTest
    @MethodSource("lossSource")
    fun `test losses`(loss: Loss, expected: String) {
        lossToCode.makeNewLoss(loss) shouldBe expected
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun lossSource() = listOf(
            Arguments.of(
                Loss.SparseCategoricalCrossentropy,
                """tf.keras.losses.sparse_categorical_crossentropy"""
            )
        )
    }
}