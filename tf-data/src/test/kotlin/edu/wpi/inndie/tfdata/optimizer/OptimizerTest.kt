package edu.wpi.inndie.tfdata.optimizer

import io.kotlintest.shouldBe
import kotlin.random.Random
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class OptimizerTest {

    @ParameterizedTest
    @MethodSource("optimizerSource")
    fun `test serialization`(optimizer: Optimizer) {
        Optimizer.deserialize(optimizer.serialize())
            .shouldBe(optimizer)
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun optimizerSource() = listOf(
            Arguments.of(
                Optimizer.Adam(
                    Random.nextDouble(),
                    Random.nextDouble(),
                    Random.nextDouble(),
                    Random.nextDouble(),
                    Random.nextBoolean()
                )
            )
        )
    }
}
