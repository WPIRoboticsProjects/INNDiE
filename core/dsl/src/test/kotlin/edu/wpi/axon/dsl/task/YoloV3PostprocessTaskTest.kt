package edu.wpi.axon.dsl.task

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.isTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class YoloV3PostprocessTaskTest {

    private val taskName = "task"

    @Test
    fun `input cannot be null`() {
        val task = YoloV3PostprocessTask(taskName).apply {
            output = configuredCorrectly()
        }

        assertThrows<UninitializedPropertyAccessException> { task.isConfiguredCorrectly() }
    }

    @Test
    fun `output cannot be null`() {
        val task = YoloV3PostprocessTask(taskName).apply {
            input = configuredCorrectly()
        }

        assertThrows<UninitializedPropertyAccessException> { task.isConfiguredCorrectly() }
    }

    @Test
    fun `configured correctly when all parameters are non-null`() {
        val task = YoloV3PostprocessTask(taskName).apply {
            input = configuredCorrectly()
            output = configuredCorrectly()
        }

        assertThat(task.isConfiguredCorrectly(), isTrue())
    }
}
