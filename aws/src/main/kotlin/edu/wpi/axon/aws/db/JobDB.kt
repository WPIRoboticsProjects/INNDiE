package edu.wpi.axon.aws.db

import arrow.fx.IO
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress

interface JobDB {

    fun createNewJob(job: Job): IO<Unit>

    fun updateJobStatus(name: String, newStatus: TrainingScriptProgress): IO<Job>

    fun deleteTable(): IO<Unit>
}
