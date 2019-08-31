package edu.wpi.axon.dsl.task

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.dsl.alwaysValidImportValidator
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.dsl.configuredIncorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.testutil.isFalse
import edu.wpi.axon.testutil.isTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class InferenceTaskTest : KoinTestFixture() {

    private val inferenceTaskName = "task"

    @Test
    fun `input cannot be uninitialized`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = InferenceTask(inferenceTaskName).apply {
            inferenceSession = configuredCorrectly()
            output = configuredCorrectly()
        }

        assertThrows<UninitializedPropertyAccessException> { task.isConfiguredCorrectly() }
    }

    @Test
    fun `inferenceSession cannot be uninitialized`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = InferenceTask(inferenceTaskName).apply {
            input = configuredCorrectly()
            output = configuredCorrectly()
        }

        assertThrows<UninitializedPropertyAccessException> { task.isConfiguredCorrectly() }
    }

    @Test
    fun `output cannot be uninitialized`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = InferenceTask(inferenceTaskName).apply {
            input = configuredCorrectly()
            inferenceSession = configuredCorrectly()
        }

        assertThrows<UninitializedPropertyAccessException> { task.isConfiguredCorrectly() }
    }

    @Test
    fun `all inputs must be configured correctly`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = InferenceTask(inferenceTaskName).apply {
            input = configuredCorrectly()
            inferenceSession = configuredIncorrectly()
            output = configuredCorrectly()
        }

        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `all outputs must be configured correctly`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = InferenceTask(inferenceTaskName).apply {
            input = configuredCorrectly()
            inferenceSession = configuredCorrectly()
            output = configuredIncorrectly()
        }

        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `configured correctly when all parameters are non-null and all inputs and outputs are configured correctly`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = InferenceTask(inferenceTaskName).apply {
            input = configuredCorrectly()
            inferenceSession = configuredCorrectly()
            output = configuredCorrectly()
        }

        assertThat(task.isConfiguredCorrectly(), isTrue())
    }

    @Test
    fun `test code`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = InferenceTask(inferenceTaskName).apply {
            input = configuredCorrectly("input1")
            inferenceSession = configuredCorrectly("input2")
            output = configuredCorrectly("output1")
        }

        assertEquals("output1 = input2.run(None, input1)", task.code())
    }
}
