package edu.wpi.axon.awsdb

import arrow.fx.IO

interface JobDB {

    fun createNewJob(job: Job): IO<Unit>

    fun deleteTable(): IO<Unit>
}
