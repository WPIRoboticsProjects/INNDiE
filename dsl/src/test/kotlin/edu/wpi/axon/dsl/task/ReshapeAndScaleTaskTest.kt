package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class ReshapeAndScaleTaskTest : KoinTestFixture() {

    @Test
    fun `test with no scale`() {
        startKoin { }

        val task = ReshapeAndScaleTask("").apply {
            input = configuredCorrectly("input")
            output = configuredCorrectly("output")
            reshapeArgs = listOf(-1, 28, 28, 1)
            scale = null
        }

        task.code() shouldBe """
            |output = input.reshape(-1, 28, 28, 1)
        """.trimMargin()
    }

    @Test
    fun `test with scale`() {
        startKoin { }

        val task = ReshapeAndScaleTask("").apply {
            input = configuredCorrectly("input")
            output = configuredCorrectly("output")
            reshapeArgs = listOf(-1, 28, 28, 1)
            scale = 255
        }

        task.code() shouldBe """
            |output = input.reshape(-1, 28, 28, 1) / 255
        """.trimMargin()
    }
}
