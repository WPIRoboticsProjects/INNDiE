package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.alwaysValidImportValidator
import edu.wpi.axon.dsl.alwaysValidPathValidator
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class MakeNewInferenceSessionTest : KoinTestFixture() {

    @Test
    fun `test code`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
                alwaysValidPathValidator()
            })
        }

        val task = MakeNewInferenceSession("task").apply {
            modelPathInput = "path1"
            sessionOutput = configuredCorrectly("var1")
        }

        assertEquals("var1 = onnxruntime.InferenceSession('path1')", task.code())
    }
}
