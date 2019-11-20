package edu.wpi.axon.tfdata.loss

import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class LossTest {

    @ParameterizedTest
    @MethodSource("lossSource")
    fun `test serialization`(loss: Loss) {
        Loss.deserialize(loss.serialize()).shouldBe(loss)
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun lossSource() = listOf(
            Arguments.of(Loss.SparseCategoricalCrossentropy)
        )
    }
}
