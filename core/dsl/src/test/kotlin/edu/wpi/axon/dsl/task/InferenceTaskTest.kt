package edu.wpi.axon.dsl.task

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.testutil.isFalse
import edu.wpi.axon.testutil.isTrue
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class InferenceTaskTest {

    private val inferenceTaskName = "task"

    @Test
    fun `input cannot be null`() {
        val task = InferenceTask(inferenceTaskName).apply {
            inferenceSession = mockk()
            output = mockk()
        }

        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `inferenceSession cannot be null`() {
        val task = InferenceTask(inferenceTaskName).apply {
            input = mockk()
            output = mockk()
        }

        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `output cannot be null`() {
        val task = InferenceTask(inferenceTaskName).apply {
            input = mockk()
            inferenceSession = mockk()
        }

        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `all inputs must be configured correctly`() {
        val task = InferenceTask(inferenceTaskName).apply {
            input = mockk { every { isConfiguredCorrectly() } returns true }
            inferenceSession = mockk { every { isConfiguredCorrectly() } returns false }
            output = mockk { every { isConfiguredCorrectly() } returns true }
        }

        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `all outputs must be configured correctly`() {
        val task = InferenceTask(inferenceTaskName).apply {
            input = mockk { every { isConfiguredCorrectly() } returns true }
            inferenceSession = mockk { every { isConfiguredCorrectly() } returns true }
            output = mockk { every { isConfiguredCorrectly() } returns false }
        }

        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `configured correctly when all parameters are non-null and all inputs and outputs are configured correctly`() {
        val task = InferenceTask(inferenceTaskName).apply {
            input = mockk { every { isConfiguredCorrectly() } returns true }
            inferenceSession = mockk { every { isConfiguredCorrectly() } returns true }
            output = mockk { every { isConfiguredCorrectly() } returns true }
        }

        assertThat(task.isConfiguredCorrectly(), isTrue())
    }
}
