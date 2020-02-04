package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.createLocalProgressFilepath
import io.kotlintest.shouldBe
import java.io.File
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class LocalTrainingScriptProgressReporterTest {

    @Test
    fun `in-progress reporting after restart`(@TempDir tempDir: File) {
        val id = 1

        // Initialize the progress file
        File(createLocalProgressFilepath(tempDir.path, id)).apply {
            parentFile.mkdirs()
            createNewFile()
            writeText("5")
        }

        val reporter = LocalTrainingScriptProgressReporter(tempDir.path)
        reporter.addJobAfterRestart(
            RunTrainingScriptConfiguration(
                FilePath.Local("old.h5"),
                Dataset.ExampleDataset.Mnist,
                "",
                10,
                tempDir.toPath(),
                id
            )
        )

        reporter.getTrainingProgress(id).shouldBe(TrainingScriptProgress.InProgress(0.5))
    }

    @Test
    fun `progress reporting after restart should reach completed`(@TempDir tempDir: File) {
        val id = 1

        // Initialize the progress file
        File(createLocalProgressFilepath(tempDir.path, id)).apply {
            parentFile.mkdirs()
            createNewFile()
            writeText("10")
        }

        val reporter = LocalTrainingScriptProgressReporter(tempDir.path)
        reporter.addJobAfterRestart(
            RunTrainingScriptConfiguration(
                FilePath.Local("old.h5"),
                Dataset.ExampleDataset.Mnist,
                "",
                10,
                tempDir.toPath(),
                id
            )
        )

        reporter.getTrainingProgress(id).shouldBe(TrainingScriptProgress.Completed)
    }

    @Test
    fun `progress reporting after restart with invalid progress contents`(@TempDir tempDir: File) {
        val id = 1

        // Initialize the progress file
        File(createLocalProgressFilepath(tempDir.path, id)).apply {
            parentFile.mkdirs()
            createNewFile()
            writeText(RandomStringUtils.randomAlphanumeric(10))
        }

        val reporter = LocalTrainingScriptProgressReporter(tempDir.path)
        reporter.addJobAfterRestart(
            RunTrainingScriptConfiguration(
                FilePath.Local("old.h5"),
                Dataset.ExampleDataset.Mnist,
                "",
                10,
                tempDir.toPath(),
                id
            )
        )

        reporter.getTrainingProgress(id)::class.shouldBe(TrainingScriptProgress.Error::class)
    }
}
