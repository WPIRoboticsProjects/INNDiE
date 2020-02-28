package edu.wpi.axon.ui

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.DesiredJobTrainingMethod
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.TrainingScriptProgress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.koin.core.KoinComponent

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

    /**
     * Synchronizes the state of this manager with the database. Resumes progress tracking for any
     * Jobs that were running when Axon was last closed.
     */
    fun initialize() {
        val runningJobs = jobDb.fetchRunningJobs()
        runningJobs.forEach { job ->
            scope.launch {
                jobRunner.startProgressReporting(job)
                LOGGER.debug { "Waiting for Job ${job.id} to finish." }
                jobRunner.waitForFinish(job.id) {
                    jobDb.update(job.id, status = it)
                }
            }
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

    /**
     * Generates the code for a job and starts it.
     *
     * @param job The [Job] to run.
     * @return The coroutine Job that was started.
     */
    fun startJob(job: Job, desiredJobTrainingMethod: DesiredJobTrainingMethod) {
        scope.launch {
            jobDb.update(job.id, status = TrainingScriptProgress.Creating)

            val trainingMethod = jobRunner.startJob(job, desiredJobTrainingMethod)
            jobDb.update(job.id, internalJobTrainingMethod = trainingMethod)
            LOGGER.debug { "Started job with id: ${job.id}" }

            delay(waitAfterStartingJobMs)

            jobRunner.waitForFinish(job.id) {
                jobDb.update(job.id, status = it)
            }
        }.invokeOnCompletion {
            if (it != null && it !is CancellationException) {
                // The coroutine ended exceptionally
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
    fun cancelJob(id: Int) = jobRunner.cancelJob(id)

    fun listResults(id: Int) = jobRunner.listResults(id)

    fun getResult(id: Int, filename: String) = jobRunner.getResult(id, filename)

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
