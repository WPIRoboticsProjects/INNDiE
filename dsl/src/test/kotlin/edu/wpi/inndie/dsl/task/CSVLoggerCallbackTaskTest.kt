package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.alwaysValidImportValidator
import edu.wpi.inndie.dsl.configuredCorrectly
import edu.wpi.inndie.testutil.KoinTestFixture
import io.kotlintest.matchers.booleans.shouldBeFalse
import io.kotlintest.matchers.booleans.shouldBeTrue
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

class CSVLoggerCallbackTaskTest : KoinTestFixture() {

    @Test
    fun `test invalid logFilePath`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = CSVLoggerCallbackTask("").apply {
            logFilePath = ""
            separator = ','
            append = false
            output = configuredCorrectly("output")
        }
        task.isConfiguredCorrectly().shouldBeFalse()
    }

    @Test
    fun `test code gen`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = CSVLoggerCallbackTask("").apply {
            logFilePath = "logFile.csv"
            separator = ','
            append = false
            output = configuredCorrectly("output")
        }
        task.isConfiguredCorrectly().shouldBeTrue()
        task.code().shouldBe(
            """
            |output = tf.keras.callbacks.CSVLogger(
            |    "logFile.csv",
            |    separator=',',
            |    append=False
            |)
            """.trimMargin()
        )
    }
}
