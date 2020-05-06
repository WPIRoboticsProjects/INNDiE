package edu.wpi.inndie.aws

import edu.wpi.inndie.db.data.TrainingScriptProgress
import edu.wpi.inndie.tfdata.Dataset
import edu.wpi.inndie.util.FilePath
import edu.wpi.inndie.util.createLocalProgressFilepath
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
        createLocalProgressFilepath(tempDir.toPath()).toFile().apply {
            parentFile.mkdirs()
            createNewFile()
            writeText(
                """
                epoch
                0
                1
                2
                3
                4
                5
            """.trimIndent()
            )
        }

        val reporter = LocalTrainingScriptProgressReporter()
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

        reporter.getTrainingProgress(id).shouldBe(
            TrainingScriptProgress.InProgress(
                0.5,
                """
                epoch
                0
                1
                2
                3
                4
                5
                """.trimIndent()
            )
        )
    }

    @Test
    fun `progress reporting after restart should reach completed`(@TempDir tempDir: File) {
        val id = 1

        // Initialize the progress file
        createLocalProgressFilepath(tempDir.toPath()).toFile().apply {
            parentFile.mkdirs()
            createNewFile()
            writeText(
                """
                epoch
                4
                10
                """.trimIndent()
            )
        }

        val reporter = LocalTrainingScriptProgressReporter()
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
        createLocalProgressFilepath(tempDir.toPath()).toFile().apply {
            parentFile.mkdirs()
            createNewFile()
            writeText(RandomStringUtils.randomAlphanumeric(10))
        }

        val reporter = LocalTrainingScriptProgressReporter()
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
