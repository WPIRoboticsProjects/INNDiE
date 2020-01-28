package edu.wpi.axon.ui

import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.dbdata.nextJob
import edu.wpi.axon.util.FilePath
import io.kotlintest.shouldThrow
import kotlin.random.Random
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class JobRunnerTest {

    private val runner = JobRunner()

    @ParameterizedTest
    @MethodSource("runningJobTestSource")
    fun `test starting a running job`(status: TrainingScriptProgress) {
        shouldThrow<IllegalArgumentException> {
            runner.startJob(Random.nextJob().copy(status = status))
        }
    }

    @Test
    fun `test starting a job where usesAWS failed`() {
        shouldThrow<IllegalArgumentException> {
            runner.startJob(
                Random.nextJob().copy(
                    userOldModelPath = FilePath.S3(""),
                    userNewModelName = FilePath.Local("")
                )
            )
        }
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun runningJobTestSource() = listOf(
            TrainingScriptProgress.Creating,
            TrainingScriptProgress.Initializing,
            TrainingScriptProgress.InProgress(0.2)
        )
    }
}
