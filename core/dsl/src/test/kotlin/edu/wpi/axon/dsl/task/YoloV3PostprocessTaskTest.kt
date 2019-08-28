package edu.wpi.axon.dsl.task

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.dsl.isFalse
import edu.wpi.axon.dsl.isTrue
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class YoloV3PostprocessTaskTest {

    private val taskName = "task"

    @Test
    fun `input cannot be null`() {
        val task = YoloV3PostprocessTask(taskName).apply {
            output = mockk()
        }

        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `output cannot be null`() {
        val task = YoloV3PostprocessTask(taskName).apply {
            input = mockk()
        }

        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `configured correctly when all parameters are non-null`() {
        val task = YoloV3PostprocessTask(taskName).apply {
            input = mockk()
            output = mockk()
        }

        assertThat(task.isConfiguredCorrectly(), isTrue())
    }
}
