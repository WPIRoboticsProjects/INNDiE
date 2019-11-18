package edu.wpi.axon.aws.db

import com.beust.klaxon.Klaxon
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object Jobs : IntIdTable() {
    val name = varchar("name", 255).uniqueIndex()
    val status = varchar("status", 255)
    val userOldModelPath = varchar("userOldModelPath", 255)
    val userNewModelName = varchar("userNewModelName", 255)
    val userDataset = varchar("dataset", 255)
    val userOptimizer = varchar("userOptimizer", 255)
    val userLoss = varchar("userLoss", 255)
    val userMetrics = varchar("userMetrics", 255)
    val userEpochs = integer("userEpochs")
    val generateDebugComments = bool("generateDebugComments")
}

class JobEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<JobEntity>(Jobs) {
        private val klaxon = Klaxon()
    }

    var name by Jobs.name
    var status by Jobs.status
    var userOldModelPath by Jobs.userOldModelPath
    var userNewModelName by Jobs.userNewModelName
    var userDataset by Jobs.userDataset
    var userOptimizer by Jobs.userOptimizer
    var userLoss by Jobs.userLoss
    var userMetrics by Jobs.userMetrics
    var userEpochs by Jobs.userEpochs
    var generateDebugComments by Jobs.generateDebugComments

    fun toJob() = Job(
        name = name,
        status = TrainingScriptProgress.deserialize(status),
        userOldModelPath = userOldModelPath,
        userNewModelName = userNewModelName,
        userDataset = Dataset.deserialize(userDataset),
        userOptimizer = Optimizer.deserialize(userOptimizer),
        userLoss = Loss.deserialize(userLoss),
        userMetrics = klaxon.parseArray<String>(userMetrics)!!.toSet(),
        userEpochs = userEpochs,
        generateDebugComments = generateDebugComments,
        id = id.value.toLong()
    )
}

class DefaultJobDB(private val database: Database) : JobDB {

    private val klaxon = Klaxon()

    init {
        transaction(database) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Jobs)
        }
    }

    override fun putJob(job: Job): Unit = transaction(database) {
        JobEntity.new {
            name = job.name
            status = job.status.serialize()
            userOldModelPath = job.userOldModelPath
            userNewModelName = job.userNewModelName
            userDataset = job.userDataset.serialize()
            userOptimizer = job.userOptimizer.serialize()
            userLoss = job.userLoss.serialize()
            userMetrics = klaxon.toJsonString(job.userMetrics)
            userEpochs = job.userEpochs
            generateDebugComments = job.generateDebugComments
        }
    }

    override fun getJobById(jobId: Int): Job? = transaction(database) {
        JobEntity.findById(jobId)?.toJob()
    }

    override fun updateJobStatus(jobId: Int, newStatus: TrainingScriptProgress): Unit =
        transaction(database) {
            JobEntity.findById(jobId)!!.status = newStatus.serialize()
        }

    override fun getJobsWithStatus(status: TrainingScriptProgress): List<Job> =
        transaction(database) {
            val serializedStatus = status.serialize()
            JobEntity.find { Jobs.status eq serializedStatus }.map { it.toJob() }
        }
}
