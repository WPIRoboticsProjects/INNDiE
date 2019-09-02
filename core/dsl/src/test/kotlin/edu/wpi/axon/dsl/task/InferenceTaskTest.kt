package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.alwaysValidImportValidator
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class InferenceTaskTest : KoinTestFixture() {

    @Test
    fun `test code`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = InferenceTask("task").apply {
            input = configuredCorrectly("input1")
            inferenceSession = configuredCorrectly("input2")
            output = configuredCorrectly("output1")
        }

        assertEquals("output1 = input2.run(None, input1)", task.code())
    }
}
