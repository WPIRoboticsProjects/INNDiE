package edu.wpi.inndie.testrunner

import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.plugin.Plugin
import edu.wpi.inndie.testutil.KoinTestFixture
import io.kotlintest.assertions.arrow.either.shouldBeLeft
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.file.shouldContainFiles
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.context.startKoin

internal class LocalTestRunnerIntegTest : KoinTestFixture() {

    @Test
    fun `dummy test`() {
        // Each test class needs at least one test or else JUnit complains (disabled tests don't
        // count).
    }

    @Test
    @Tag("needsDocker")
    fun `running a script with errors returns a left`(@TempDir tempDir: File) {
        startKoin { modules(defaultBackendModule()) }

        val modelPath =
            Path.of(this::class.java.getResource("32_32_1_conv_sequential-trained.h5").toURI())

        val runner = LocalTestRunner()
        val testResults = runner.runTest(
            modelPath,
            TestData.FromFile(modelPath), // Not meaningful data
            Plugin.Official("", ""),
            Plugin.Official("", ""),
            Paths.get(tempDir.toPath().toRealPath().toUri())
        )

        testResults.shouldBeLeft()
    }

    @Test
    @Tag("needsDocker")
    fun `running a script that emits two files returns those two files`(@TempDir tempDir: File) {
        startKoin { modules(defaultBackendModule()) }

        val modelPath =
            Path.of(this::class.java.getResource("32_32_1_conv_sequential-trained.h5").toURI())

        val runner = LocalTestRunner()
        val testResults = runner.runTest(
            modelPath,
            TestData.FromFile(modelPath), // Not meaningful data
            Plugin.Official(
                "",
                """
                |def load_test_data(input):
                |    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data()
                |    x_test = x_test[:1]
                |    x_test = tf.cast(x_test / 255, tf.float32)
                |    x_test = x_test[..., tf.newaxis]
                |    return (x_test, y_test[:1], 1)
                """.trimMargin()
            ),
            Plugin.Official(
                "",
                """
                |def process_model_output(model_input, expected_output, model_output):
                |    from pathlib import Path
                |    Path("output/file1.txt").touch()
                |    Path("output/file2.txt").touch()
                """.trimMargin()
            ),
            Paths.get(tempDir.toPath().toRealPath().toUri())
        )

        val outputDir = tempDir.toPath().toRealPath().resolve("output")
        println(outputDir.toFile().walkTopDown().joinToString("\n"))
        outputDir.shouldContainFiles("file1.txt", "file2.txt")
        testResults.shouldBeRight {
            it.shouldContainExactlyInAnyOrder(
                outputDir.resolve("file1.txt").toFile(),
                outputDir.resolve("file2.txt").toFile()
            )
        }
    }
}
