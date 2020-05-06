package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.alwaysInvalidPathValidator
import edu.wpi.inndie.dsl.alwaysValidImportValidator
import edu.wpi.inndie.dsl.alwaysValidPathValidator
import edu.wpi.inndie.dsl.configuredCorrectly
import edu.wpi.inndie.testutil.KoinTestFixture
import io.kotlintest.matchers.booleans.shouldBeFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class LoadClassLabelsTest : KoinTestFixture() {

    @Test
    fun `test code`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
                alwaysValidPathValidator()
            })
        }

        val task = LoadClassLabels("task").apply {
            classLabelsPath = "path1"
            classOutput = configuredCorrectly("var1")
        }

        assertEquals("var1 = [line.rstrip('\\n') for line in open('path1')]", task.code())
    }

    @Test
    fun `test invalid path`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
                alwaysInvalidPathValidator()
            })
        }

        val task = LoadClassLabels("task").apply {
            classLabelsPath = "path1"
            classOutput = configuredCorrectly("var1")
        }

        task.isConfiguredCorrectly().shouldBeFalse()
    }
}
