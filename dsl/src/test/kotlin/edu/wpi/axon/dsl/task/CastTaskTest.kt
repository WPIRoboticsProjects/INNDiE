package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class CastTaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin { }

        CastTask("").apply {
            input = configuredCorrectly("input")
            output = configuredCorrectly("output")
            dtype = "dtype"
        }.code().shouldBe("output = tf.cast(input, dtype)")
    }
}
