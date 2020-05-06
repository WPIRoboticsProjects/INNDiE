package edu.wpi.inndie.tfdata.code.optimizer

import edu.wpi.inndie.tfdata.optimizer.Optimizer
import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DefaultOptimizerToCodeTest {

    private val optimizerToCode = DefaultOptimizerToCode()

    @ParameterizedTest
    @MethodSource("optimizerSource")
    fun `test optimizers`(optimizer: Optimizer, expected: String) {
        optimizerToCode.makeNewOptimizer(optimizer) shouldBe expected
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun optimizerSource() = listOf(
            Arguments.of(
                Optimizer.Adam(),
                """tf.keras.optimizers.Adam(0.001, 0.9, 0.999, 1.0E-7, False)"""
            ),
            Arguments.of(
                Optimizer.FTRL(),
                """tf.keras.optimizers.Ftrl(0.001, -0.5, 0.1, 0.0, 0.0, 'Ftrl', 0.0)"""
            ),
            Arguments.of(
                Optimizer.RMSprop(),
                """tf.keras.optimizers.RMSprop(0.001, 0.9, 0.0, 1.0E-7, False)"""
            )
        )
    }
}
