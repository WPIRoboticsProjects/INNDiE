package edu.wpi.axon.aws

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.util.FilePath
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import java.nio.file.Paths

internal class LocalTrainingScriptRunnerTest {

    private val runner = LocalTrainingScriptRunner()

    @Test
    fun `test running with non-local old model`() {
        shouldThrow<IllegalArgumentException> {
            runner.startScript(
                RunTrainingScriptConfiguration(
                    FilePath.S3("a"),
                    Dataset.ExampleDataset.FashionMnist,
                    "",
                    1,
                    Paths.get("."),
                    1
                )
            )
        }
    }

    @Test
    fun `test running with zero epochs`() {
        shouldThrow<IllegalArgumentException> {
            runner.startScript(
                RunTrainingScriptConfiguration(
                    FilePath.Local("a"),
                    Dataset.ExampleDataset.FashionMnist,
                    "",
                    0,
                    Paths.get("."),
                    1
                )
            )
        }
    }

    @Test
    fun `test running with non-local dataset`() {
        shouldThrow<IllegalArgumentException> {
            runner.startScript(
                RunTrainingScriptConfiguration(
                    FilePath.Local("a"),
                    Dataset.Custom(FilePath.S3("d"), "d"),
                    "",
                    1,
                    Paths.get("."),
                    1
                )
            )
        }
    }
}
