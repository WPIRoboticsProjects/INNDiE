package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.core.right
import edu.wpi.axon.tfdata.layer.Initializer
import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DefaultInitializerToCodeTest {

    @ParameterizedTest
    @MethodSource("initializerSource")
    fun `test initializers`(initializer: Initializer, expected: Either<String, String>) {
        DefaultInitializerToCode().makeNewInitializer(initializer) shouldBe expected
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun initializerSource() = listOf(
            Arguments.of(
                Initializer.Constant(Left(0.0)),
                "tf.keras.initializers.Constant(value=0.0)".right()
            ),
            Arguments.of(
                Initializer.Identity(1.2),
                "tf.keras.initializers.Identity(gain=1.2)".right()
            ),
            Arguments.of(
                Initializer.Ones,
                "tf.keras.initializers.Ones()".right()
            ),
            Arguments.of(
                Initializer.Orthogonal(1.1, null),
                "tf.keras.initializers.Orthogonal(gain=1.1, seed=None)".right()
            ),
            Arguments.of(
                Initializer.RandomNormal(1.0, 2.0, null),
                "tf.keras.initializers.RandomNormal(mean=1.0, stddev=2.0, seed=None)".right()
            ),
            Arguments.of(
                Initializer.RandomUniform(Left(-0.05), Left(0.05), null),
                "tf.keras.initializers.RandomUniform(minval=-0.05, maxval=0.05, seed=None)".right()
            ),
            Arguments.of(
                Initializer.RandomUniform(Right(listOf(1.0, 2.0)), Right(listOf(3.0, 4.0)), null),
                "tf.keras.initializers.RandomUniform(minval=[1.0, 2.0], maxval=[3.0, 4.0], seed=None)".right()
            ),
            Arguments.of(
                Initializer.TruncatedNormal(1.0, 2.0, null),
                "tf.keras.initializers.TruncatedNormal(mean=1.0, stddev=2.0, seed=None)".right()
            ),
            Arguments.of(
                Initializer.VarianceScaling(
                    1.0,
                    Initializer.VarianceScaling.Mode.FanAvg,
                    Initializer.VarianceScaling.Distribution.UntruncatedNormal,
                    null
                ),
                """tf.keras.initializers.VarianceScaling(scale=1.0, mode="fan_avg", distribution="untruncated_normal", seed=None)""".right()
            ),
            Arguments.of(
                Initializer.Zeros,
                "tf.keras.initializers.Zeros()".right()
            ),
            Arguments.of(
                Initializer.GlorotNormal(1),
                "tf.keras.initializers.glorot_normal(seed=1)".right()
            ),
            Arguments.of(
                Initializer.GlorotUniform(1),
                "tf.keras.initializers.glorot_uniform(seed=1)".right()
            )
        )
    }
}
