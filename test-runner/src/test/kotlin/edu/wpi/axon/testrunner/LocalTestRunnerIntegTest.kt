package edu.wpi.axon.testrunner

import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.util.toPrintableString
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.file.shouldContainFiles
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.context.startKoin

internal class LocalTestRunnerIntegTest : KoinTestFixture() {

    @Test
    fun `running a script that emits two files returns those two files`(@TempDir tempDir: File) {
        startKoin { modules(defaultBackendModule()) }

        val modelPath =
            Path.of(this::class.java.getResource("32_32_1_conv_sequential-trained.h5").toURI())

        println("File.separator: ${File.separator}")
        println("File.separator == \"\\\": ${File.separator == "\\"}")
        println("File.separatorChar: ${File.separatorChar}")
        println("File.separatorChar == '\\': ${File.separatorChar == '\\'}")
        println("File.pathSeparator: ${File.pathSeparator}")
        println("File.pathSeparatorChar: ${File.pathSeparatorChar}")
        println("tempDir.toPath(): ${tempDir.toPath()}")
        println("tempDir.toPath(): ${tempDir.toPath().toPrintableString()}")
        println("tempDir.path: ${tempDir.path}")
        println("tempDir.name: ${tempDir.name}")
        println("tempDir.parent: ${tempDir.parent}")
        println("tempDir.toPath().nameCount: ${tempDir.toPath().nameCount}")
        println("tempDir.toPath().toUri(): ${tempDir.toPath().toUri()}")
        println("tempDir.toPath().toAbsolutePath().toUri(): ${tempDir.toPath().toAbsolutePath().toUri()}")
        println("tempDir.toPath().toRealPath().toUri(): ${tempDir.toPath().toRealPath().toUri()}")
        println("Paths.get(tempDir.toPath().toUri()): ${Paths.get(tempDir.toPath().toUri())}")
        println("Paths.get(tempDir.toPath().toAbsolutePath().toUri()): ${Paths.get(tempDir.toPath().toAbsolutePath().toUri())}")
        println("Paths.get(tempDir.toPath().toUri()).toAbsolutePath(): ${Paths.get(tempDir.toPath().toUri()).toAbsolutePath()}")
        println("Paths.get(tempDir.toPath().toUri()).toRealPath(): ${Paths.get(tempDir.toPath().toUri()).toRealPath()}")
        println("tempDir.toPath().toRealPath(): ${tempDir.toPath().toRealPath()}")
        println("tempDir.toPath().toAbsolutePath(): ${tempDir.toPath().toAbsolutePath()}")
        println("Paths.get(tempDir.toPath().toRealPath().toUri()): ${Paths.get(tempDir.toPath().toRealPath().toUri())}")
        println("FileSystems.getDefault().getPath(tempDir.toPath().toRealPath().toString()): ${FileSystems.getDefault().getPath(tempDir.toPath().toRealPath().toString())}")

        val runner = LocalTestRunner()
        val testResults = runner.runTest(
            modelPath,
            modelPath,
            Plugin.Official(
                "",
                """
                |def load_test_data(path):
                |    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data()
                |    x_train = tf.cast(x_train / 255, tf.float32)
                |    x_train = x_train[..., tf.newaxis]
                |    return (x_train, 1)
                """.trimMargin()
            ),
            Plugin.Official(
                "",
                """
                |def process_model_output(model_input, model_output):
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
        testResults.shouldContainExactlyInAnyOrder(
            outputDir.resolve("file1.txt").toFile(),
            outputDir.resolve("file2.txt").toFile()
        )
    }
}
