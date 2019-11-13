package edu.wpi.axon.aws.db

import arrow.fx.IO
import edu.wpi.axon.dbdata.Job

interface JobDB {

    fun createNewJob(job: Job): IO<Unit>

    fun deleteTable(): IO<Unit>
}
