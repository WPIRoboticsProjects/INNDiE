package edu.wpi.axon.ui

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.dbdata.nextJob
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.random.Random
import org.junit.jupiter.api.Test

internal class JobLifecycleManagerTest {

    private val waitAfterStaringJobMs = 1L

    @Test
    fun `test starting job`() {
        val jobRunner = mockk<JobRunner> {
            every { startJob(any()) } returns Unit
            coEvery { waitForFinish(any(), any()) } returns Unit
        }

        val jobDb = mockk<JobDb> {
            every { update(any()) } returns Unit
        }

        val job = Random.nextJob().copy(id = Random.nextInt(1, Int.MAX_VALUE))
        val jobLifecycleManager = JobLifecycleManager(jobRunner, jobDb, waitAfterStaringJobMs)
        jobLifecycleManager.startJob(job)

        // Give the coroutine inside startJob time to run
        Thread.sleep(500)

        verify(exactly = 1) { jobRunner.startJob(job) }
        verify(atLeast = 1) { jobDb.update(any()) }
        coVerify(exactly = 1) { jobRunner.waitForFinish(eq(job.id), any()) }
    }

    @Test
    fun `test cancelling job`() {
        val jobRunner = mockk<JobRunner> {
            every { cancelJob(any()) } returns Unit
        }
        val jobDb = mockk<JobDb> { }

        val id = Random.nextInt(1, Int.MAX_VALUE)
        val jobLifecycleManager = JobLifecycleManager(jobRunner, jobDb, waitAfterStaringJobMs)
        jobLifecycleManager.cancelJob(id)

        verify(exactly = 1) { jobRunner.cancelJob(id) }
    }

    @Test
    fun `test resuming progress tracking from restart`() {
        val job1 = Random.nextJob().copy(
            status = TrainingScriptProgress.Initializing,
            id = Random.nextInt(1, Int.MAX_VALUE)
        )

        val job2 = Random.nextJob().copy(
            status = TrainingScriptProgress.InProgress(0.2),
            id = Random.nextInt(1, Int.MAX_VALUE)
        )

        val jobRunner = mockk<JobRunner> {
            coEvery { waitForFinish(any(), any()) } returns Unit
        }

        val jobDb = mockk<JobDb> {
            every { fetchRunningJobs() } returns listOf(job1, job2)
        }

        val jobLifecycleManager = JobLifecycleManager(jobRunner, jobDb, waitAfterStaringJobMs)
        jobLifecycleManager.initialize()

        verify(exactly = 1) { jobDb.fetchRunningJobs() }
        coVerifyAll {
            jobRunner.waitForFinish(eq(job1.id), any())
            jobRunner.waitForFinish(eq(job2.id), any())
        }
    }
}
