package edu.wpi.axon.core.dsl.task

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.core.isFalse
import edu.wpi.axon.core.isTrue
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
    fun `configured correctly when all parameters are non-null`() {
        val task = InferenceTask(inferenceTaskName).apply {
            input = mockk()
            inferenceSession = mockk()
            output = mockk()
        }

        assertThat(task.isConfiguredCorrectly(), isTrue())
    }
}
