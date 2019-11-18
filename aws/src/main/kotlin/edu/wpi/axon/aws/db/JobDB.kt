package edu.wpi.axon.aws.db

import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress

interface JobDB {

    /**
     * Puts (create or replace) the [job] in the DB.
     *
     * @param job The new [Job].
     * @return An effect for continuation.
     */
    fun putJob(job: Job)

    /**
     * Gets a job by its id.
     *
     * @param jobId The id of the [Job].
     * @return The [Job] or `null` if there was no matching [Job].
     */
    fun getJobById(jobId: Int): Job?

    /**
     * Updates the status of a job in the DB.
     *
     * @param job The [Job] to update (with the old status).
     * @param newStatus The new status.
     * @return The new [Job] (with the [newStatus]).
     */
    fun updateJobStatus(jobId: Int, newStatus: TrainingScriptProgress)

    /**
     * Gets all [Job]s with the [status].
     *
     * @param status The status.
     * @return The matching [Job]s.
     */
    fun getJobsWithStatus(status: TrainingScriptProgress): List<Job>
}
