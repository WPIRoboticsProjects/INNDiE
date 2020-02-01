package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class SliceTaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin { }

        SliceTask("").apply {
            input = configuredCorrectly("input")
            output = configuredCorrectly("output")
            sliceNotation = "[start:end:step]"
        }.code().shouldBe("output = input[start:end:step]")
    }
}
