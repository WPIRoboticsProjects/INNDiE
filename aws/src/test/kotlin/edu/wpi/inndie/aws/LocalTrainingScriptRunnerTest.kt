package edu.wpi.inndie.aws

import edu.wpi.inndie.tfdata.Dataset
import edu.wpi.inndie.util.FilePath
import io.kotlintest.shouldThrow
import java.io.File
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class LocalTrainingScriptRunnerTest {

    private val runner = LocalTrainingScriptRunner()

    @Test
    fun `test running with non-local old model`(@TempDir tempDir: File) {
        shouldThrow<IllegalArgumentException> {
            runner.startScript(
                RunTrainingScriptConfiguration(
                    FilePath.S3("a"),
                    Dataset.ExampleDataset.FashionMnist,
                    "",
                    1,
                    tempDir.toPath(),
                    1
                )
            )
        }
    }

    @Test
    fun `test running with zero epochs`(@TempDir tempDir: File) {
        shouldThrow<IllegalArgumentException> {
            runner.startScript(
                RunTrainingScriptConfiguration(
                    FilePath.Local("a"),
                    Dataset.ExampleDataset.FashionMnist,
                    "",
                    0,
                    tempDir.toPath(),
                    1
                )
            )
        }
    }

    @Test
    fun `test running with non-local dataset`(@TempDir tempDir: File) {
        shouldThrow<IllegalArgumentException> {
            runner.startScript(
                RunTrainingScriptConfiguration(
                    FilePath.Local("a"),
                    Dataset.Custom(FilePath.S3("d"), "d"),
                    "",
                    1,
                    tempDir.toPath(),
                    1
                )
            )
        }
    }
}
