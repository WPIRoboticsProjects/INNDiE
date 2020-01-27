package edu.wpi.axon.ui

import arrow.core.None
import arrow.core.Option
import edu.wpi.axon.aws.TrainingScriptRunner
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.util.axonBucketName
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal class JobRunnerTest : KoinTestFixture() {

    @Test
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
                single<Option<String>>(named(axonBucketName)) { None }
                single { mockTrainingScriptRunner }
            })
        }

        val jobRunner = JobRunner(1)
        jobRunner.waitForFinish(id) {}.unsafeRunSync()

        verifyAll {
            mockTrainingScriptRunner.getTrainingProgress(id)
            mockTrainingScriptRunner.getTrainingProgress(id)
        }
    }
}
