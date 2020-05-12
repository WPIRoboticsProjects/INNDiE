package edu.wpi.inndie.tfdata.code.layer

import arrow.core.Either
import arrow.core.right
import edu.wpi.inndie.tfdata.layer.Regularizer
import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DefaultRegularizerToCodeTest {

    @ParameterizedTest
    @MethodSource("regularizerSource")
    fun `test regularizers`(regularizer: Regularizer, expected: Either<String, String>) {
        DefaultRegularizerToCode().makeNewRegularizer(regularizer) shouldBe expected
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun regularizerSource() = listOf(
            Arguments.of(
                Regularizer.L1L2(0.01, 0.02),
                """tf.keras.regularizers.L1L2(0.01, 0.02)""".right(),
                null
            )
        )
    }
}
