package edu.wpi.axon.ui

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.DesiredJobTrainingMethod
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.TrainingScriptProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.koin.core.KoinComponent

private class JobCancelledByUserException : Throwable()

/**
 * Manages the entire lifecycle of a Job. Starting, tracking progress, cancelling, resuming progress
 * updates between restarts, etc.
 *
 * @param jobRunner Used to manage running/cancelling Jobs.
 * @param jobDb Used to pull running Jobs from during [initialize].
 * @param waitAfterStartingJobMs How long to wait after starting a Job before calling
 * [JobRunner.waitForFinish] to ensure the Job has time to start.
 */
class JobLifecycleManager internal constructor(
    private val jobRunner: JobRunner,
    private val jobDb: JobDb,
    private val waitAfterStartingJobMs: Long
) : KoinComponent {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val jobJobs = mutableMapOf<Int, kotlinx.coroutines.Job>()

    /**
     * Synchronizes the state of this manager with the database. Resumes progress tracking for any
     * Jobs that were running when Axon was last closed.
     */
    fun initialize() {
        val runningJobs = jobDb.fetchRunningJobs()
        LOGGER.debug { "Running jobs:\n${runningJobs.joinToString("\n")}" }
        runningJobs.forEach { job ->
            val jobJob = scope.launch {
                jobRunner.startProgressReporting(job)
                LOGGER.debug { "Waiting for Job ${job.id} to finish." }
                jobRunner.waitForFinish(job.id) {
                    jobDb.update(job.id, status = it)
                }
            }

            setInvokeOnCompletion(jobJob, job)
            jobJobs[job.id] = jobJob
        }
    }

    /**
     * Generates the code for a job and starts it.
     *
     * @param jobId The [Job.id] of the Job to start.
     * @return The coroutine Job that was started.
     */
    fun startJob(jobId: Int, desiredJobTrainingMethod: DesiredJobTrainingMethod) =
        startJob(jobDb.getById(jobId)!!, desiredJobTrainingMethod)

    private fun startJob(job: Job, desiredJobTrainingMethod: DesiredJobTrainingMethod) {
        val jobJob = scope.launch {
            jobDb.update(job.id, status = TrainingScriptProgress.Creating)

            val trainingMethod = jobRunner.startJob(job, desiredJobTrainingMethod)
            jobDb.update(job.id, internalJobTrainingMethod = trainingMethod)
            LOGGER.debug { "Started job with id: ${job.id}" }

            delay(waitAfterStartingJobMs)

            jobRunner.waitForFinish(job.id) {
                jobDb.update(job.id, status = it)
            }
        }

        setInvokeOnCompletion(jobJob, job)

        LOGGER.debug { "Started job with id ${job.id}" }
        jobJobs[job.id] = jobJob
    }

    private fun setInvokeOnCompletion(jobJob: kotlinx.coroutines.Job, job: Job) {
        jobJob.invokeOnCompletion {
            if (it == null) {
                LOGGER.debug { "Job ${job.id} completed." }
            } else {
                LOGGER.debug(it) { "Job ${job.id} was cancelled." }

                // Try to cancel the Job in case there was a bug that caused this cancellation.
                // If there was a bug then this isn't guaranteed to work correctly, but cancelling a
                // Job is idempotent so we might as well try.
                jobRunner.cancelJob(job.id)

                jobDb.update(
                    job.id,
                    status = TrainingScriptProgress.Error(it.localizedMessage)
                )
            }
        }
    }

    /**
     * Cancels the Job. If the Job is currently running, it is interrupted. If the Job is not
     * running, this method does nothing.
     *
     * @param id The id of the Job to cancel.
     */
    fun cancelJob(id: Int) {
        LOGGER.debug { "Cancelling job id $id" }
        scope.launch {
            val jobJob = jobJobs[id]!!
            while (jobJob.isActive) {
                // If the Job job is active, then it should start the Job eventually, so we can keep
                // polling here without risk of an infinite loop.
                if (jobRunner.cancelJob(id)) {
                    jobJob.cancel("Cancelled by user.", JobCancelledByUserException())
                    break
                } else {
                    delay(500L)
                }
            }
        }
    }

    fun listResults(id: Int) = jobRunner.listResults(id)

    fun getResult(id: Int, filename: String) = jobRunner.getResult(id, filename)

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
