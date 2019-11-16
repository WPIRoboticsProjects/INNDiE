package edu.wpi.axon.aws.db

import arrow.fx.IO
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress

interface JobDB {

    /**
     * Puts (create or replace) the [job] in the DB.
     *
     * @param job The new [Job].
     * @return An effect for continuation.
     */
    fun putJob(job: Job): IO<Unit>

    /**
     * Updates the status of a job in the DB.
     *
     * @param job The [Job] to update (with the old status).
     * @param newStatus The new status.
     * @return The new [Job] (with the [newStatus]).
     */
    fun updateJobStatus(job: Job, newStatus: TrainingScriptProgress): IO<Job>

    /**
     * Gets a job with a [name].
     *
     * @param name The name of the [Job].
     * @return The [Job].
     */
    fun getJobWithName(name: String): IO<Job>

    /**
     * Gets all [Job]s with the [status].
     *
     * @param status The status.
     * @return The matching [Job]s.
     */
    fun getJobsWithStatus(status: TrainingScriptProgress): IO<List<Job>>
}
