package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import edu.wpi.axon.tfdata.layer.Regularizer
import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DefaultRegularizerToCodeTest {

    @ParameterizedTest
    @MethodSource("regularizerSource")
    fun `test regularizers`(regularizer: Regularizer?, expected: Either<String, String>) {
        DefaultRegularizerToCode().makeNewRegularizer(regularizer) shouldBe expected
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun regularizerSource() = listOf(
            Arguments.of(
                Regularizer.L1(0.01),
                """tf.keras.regularizers.l1(0.01)""".right(),
                null
            ),
            Arguments.of(
                Regularizer.L2(0.01),
                """tf.keras.regularizers.l2(0.01)""".right(),
                null
            ),
            Arguments.of(
                Regularizer.L1L2(0.01, 0.02),
                """tf.keras.regularizers.L1L2(0.01, 0.02)""".right(),
                null
            ),
            Arguments.of(
                null,
                "Cannot make an unknown regularizer: null".left()
            )
        )
    }
}
