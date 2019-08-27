package edu.wpi.axon.core.dsl.task

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.core.isFalse
import edu.wpi.axon.core.isTrue
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class YoloV3PostprocessTaskTest {

    @Test
    fun `input cannot be null`() {
        val task = YoloV3PostprocessTask().apply {
            output = mockk()
        }

        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `output cannot be null`() {
        val task = YoloV3PostprocessTask().apply {
            input = mockk()
        }

        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `configured correctly when all parameters are non-null`() {
        val task = YoloV3PostprocessTask().apply {
            input = mockk()
            output = mockk()
        }

        assertThat(task.isConfiguredCorrectly(), isTrue())
    }
}
