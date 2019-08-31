package edu.wpi.axon.tasks.yolov3

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.dsl.alwaysValidImportValidator
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.isTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

internal class YoloV3PostprocessTaskTest : KoinTest {

    private val taskName = "task"

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun `input cannot be unconfigured`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = YoloV3PostprocessTask(taskName).apply {
            output = configuredCorrectly()
        }

        assertThrows<UninitializedPropertyAccessException> { task.isConfiguredCorrectly() }
    }

    @Test
    fun `output cannot be unconfigured`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = YoloV3PostprocessTask(taskName).apply {
            input = configuredCorrectly()
        }

        assertThrows<UninitializedPropertyAccessException> { task.isConfiguredCorrectly() }
    }

    @Test
    fun `configured correctly when all parameters are configured correctly`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = YoloV3PostprocessTask(taskName).apply {
            input = configuredCorrectly()
            output = configuredCorrectly()
        }

        assertThat(task.isConfiguredCorrectly(), isTrue())
    }
}
