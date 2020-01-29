package edu.wpi.axon.ui

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.JobTrainingMethod
import edu.wpi.axon.db.data.TrainingScriptProgress
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import kotlin.random.Random
import org.junit.jupiter.api.Test

internal class JobLifecycleManagerTest {

    private val waitAfterStaringJobMs = 1L

    @Test
    fun `test starting job`() {
        val jobRunner = mockk<JobRunner> {
            every { startJob(any()) } returns JobTrainingMethod.Local
            coEvery { waitForFinish(any(), any()) } returns Unit
        }

        val jobDb = mockk<JobDb>(relaxUnitFun = true) { }

        val jobId = Random.nextInt(1, Int.MAX_VALUE)
        val job = mockk<Job> {
            every { id } returns jobId
        }

        val jobLifecycleManager = JobLifecycleManager(jobRunner, jobDb, waitAfterStaringJobMs)
        jobLifecycleManager.startJob(job)

        // Give the coroutine inside startJob time to run
        Thread.sleep(500)

        verify(exactly = 1) { jobRunner.startJob(job) }
        verify(atLeast = 1) {
            jobDb.update(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
        coVerify(exactly = 1) { jobRunner.waitForFinish(eq(jobId), any()) }
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
        val jobId1 = Random.nextInt(1, Int.MAX_VALUE)
        val job1 = mockk<Job> {
            every { id } returns jobId1
            every { status } returns TrainingScriptProgress.Initializing
        }

        val jobId2 = Random.nextInt(1, Int.MAX_VALUE)
        val job2 = mockk<Job> {
            every { id } returns jobId2
            every { status } returns TrainingScriptProgress.InProgress(0.2)
        }

        val jobRunner = mockk<JobRunner> {
            coEvery { waitForFinish(any(), any()) } returns Unit
            every { startProgressReporting(any()) } returns Unit
        }

        val jobDb = mockk<JobDb> {
            every { fetchRunningJobs() } returns listOf(job1, job2)
        }

        val jobLifecycleManager = JobLifecycleManager(jobRunner, jobDb, waitAfterStaringJobMs)
        jobLifecycleManager.initialize()

        verify(exactly = 1) { jobDb.fetchRunningJobs() }
        coVerifyAll {
            jobRunner.waitForFinish(eq(jobId1), any())
            jobRunner.waitForFinish(eq(jobId2), any())
            jobRunner.startProgressReporting(job1)
            jobRunner.startProgressReporting(job2)
        }
    }
}
