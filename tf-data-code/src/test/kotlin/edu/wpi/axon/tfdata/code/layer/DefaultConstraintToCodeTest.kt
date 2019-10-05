package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import arrow.core.right
import edu.wpi.axon.tfdata.layer.Constraint
import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DefaultConstraintToCodeTest {

    @ParameterizedTest
    @MethodSource("constraintSource")
    fun `test constraints`(constraint: Constraint?, expected: Either<String, String>) {
        DefaultConstraintToCode().makeNewConstraint(constraint) shouldBe expected
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun constraintSource() = listOf(
            Arguments.of(
                Constraint.MaxNorm(2.0, 0),
                "tf.keras.constraints.MaxNorm(max_value=2.0, axis=0)".right()
            ),
            Arguments.of(
                Constraint.MinMaxNorm(0.0, 1.0, 1.0, 0),
                "tf.keras.constraints.MinMaxNorm(min_value=0.0, max_value=1.0, rate=1.0, axis=0)".right()
            ),
            Arguments.of(
                Constraint.NonNeg,
                "tf.keras.constraints.NonNeg()".right()
            ),
            Arguments.of(
                Constraint.UnitNorm(0),
                "tf.keras.constraints.UnitNorm(axis=0)".right()
            )
        )
    }
}
