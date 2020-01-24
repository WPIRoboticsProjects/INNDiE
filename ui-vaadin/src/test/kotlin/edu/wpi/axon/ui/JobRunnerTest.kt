package edu.wpi.axon.ui

import edu.wpi.axon.aws.TrainingScriptRunner
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.util.axonBucketName
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal class JobRunnerTest : KoinTestFixture() {

    @Test
    @Timeout(value = 1L, unit = TimeUnit.MINUTES) // This test will timeout if it fails
    fun `test waitForChange`() {
        val id = 1L

        val mockTrainingScriptRunner = mockk<TrainingScriptRunner> {
            every { getTrainingProgress(id) }.returnsMany(
                TrainingScriptProgress.NotStarted,
                TrainingScriptProgress.NotStarted,
                TrainingScriptProgress.Creating
            )
        }

        startKoin {
            modules(module {
                single<String?>(named(axonBucketName)) { null }
                single { mockTrainingScriptRunner }
            })
        }

        val jobRunner = JobRunner()
        jobRunner.waitForChange(id).unsafeRunSync()

        verifyAll {
            mockTrainingScriptRunner.getTrainingProgress(id)
            mockTrainingScriptRunner.getTrainingProgress(id)
        }
    }

    @Test
    @Timeout(value = 1L, unit = TimeUnit.MINUTES) // This test will timeout if it fails
    fun `test waitForCompleted`() {
        val id = 1L

        val mockTrainingScriptRunner = mockk<TrainingScriptRunner> {
            every { getTrainingProgress(id) }.returnsMany(
                TrainingScriptProgress.NotStarted,
                TrainingScriptProgress.InProgress(0.5),
                TrainingScriptProgress.Completed
            )
        }

        startKoin {
            modules(module {
                single<String?>(named(axonBucketName)) { null }
                single { mockTrainingScriptRunner }
            })
        }

        val jobRunner = JobRunner()
        jobRunner.waitForCompleted(id) {}.unsafeRunSync()

        verifyAll {
            mockTrainingScriptRunner.getTrainingProgress(id)
            mockTrainingScriptRunner.getTrainingProgress(id)
        }
    }
}
