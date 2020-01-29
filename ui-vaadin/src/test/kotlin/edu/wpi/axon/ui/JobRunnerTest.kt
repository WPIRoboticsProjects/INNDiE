package edu.wpi.axon.ui

import arrow.core.None
import edu.wpi.axon.db.data.TrainingScriptProgress
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class JobRunnerTest {

    private val runner = JobRunner()

    @ParameterizedTest
    @MethodSource("runningJobTestSource")
    fun `test starting a running job`(givenStatus: TrainingScriptProgress) {
        shouldThrow<IllegalArgumentException> {
            runner.startJob(
                mockk {
                    every { status } returns givenStatus
                }
            )
        }
    }

    @Test
    fun `test starting a job where usesAWS failed`() {
        shouldThrow<IllegalArgumentException> {
            runner.startJob(
                mockk {
                    every { status } returns TrainingScriptProgress.NotStarted
                    every { usesAWS } returns None
                }
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
