package edu.wpi.axon.aws.db

import arrow.fx.IO
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database

class DefaultJobDB(private val database: Database) : JobDB {

    override fun putJob(job: Job): IO<Unit> {
        TODO("not implemented")
    }

    override fun updateJobStatus(job: Job, newStatus: TrainingScriptProgress): IO<Job> {
        TODO("not implemented")
    }

    override fun getJobWithName(name: String): IO<Job> {
        TODO("not implemented")
    }

    override fun getJobsWithStatus(status: TrainingScriptProgress): IO<List<Job>> {
        TODO("not implemented")
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
