package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import arrow.core.Left
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
            )
        )
    }
}
