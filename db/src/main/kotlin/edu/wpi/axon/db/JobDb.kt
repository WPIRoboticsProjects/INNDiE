package edu.wpi.axon.db

import com.beust.klaxon.Klaxon
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

private val klaxon = Klaxon()

internal object Jobs : IntIdTable() {
    val name = varchar("name", 255).uniqueIndex()
    val status = varchar("status", 255)
    val userOldModelPath = varchar("userOldModelPath", 255)
    val userNewModelName = varchar("userNewModelName", 255)
    val userDataset = text("dataset")
    val userOptimizer = varchar("userOptimizer", 255)
    val userLoss = varchar("userLoss", 255)
    val userMetrics = varchar("userMetrics", 255)
    val userEpochs = integer("userEpochs")
    val userModel = text("userModel")
    val generateDebugComments = bool("generateDebugComments")

    fun toDomain(row: ResultRow): Job {
        return Job(
            name = row[name],
            status = TrainingScriptProgress.deserialize(row[status]),
            userOldModelPath = row[userOldModelPath],
            userNewModelName = row[userNewModelName],
            userDataset = Dataset.deserialize(row[userDataset]),
            userOptimizer = Optimizer.deserialize(row[userOptimizer]),
            userLoss = Loss.deserialize(row[userLoss]),
            userMetrics = klaxon.parseArray<String>(row[userMetrics])!!.toSet(),
            userEpochs = row[userEpochs],
            generateDebugComments = row[generateDebugComments],
            userModel = Model.deserialize(row[userModel]),
            id = row[id].value
        )
    }
}

class JobDb(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Jobs)
        }
    }

    fun create(job: Job): Int? = transaction(database) {
        Jobs.insertAndGetId { row ->
            row[name] = job.name
            row[status] = job.status.serialize()
            row[userOldModelPath] = job.userOldModelPath
            row[userNewModelName] = job.userNewModelName
            row[userDataset] = job.userDataset.serialize()
            row[userOptimizer] = job.userOptimizer.serialize()
            row[userLoss] = job.userLoss.serialize()
            row[userMetrics] = klaxon.toJsonString(job.userMetrics)
            row[userEpochs] = job.userEpochs
            row[userModel] = job.userModel.serialize()
            row[generateDebugComments] = job.generateDebugComments
        }.value
    }

    fun update(job: Job): Int? = transaction(database) {
        Jobs.update({ Jobs.id eq job.id }) {
            it[name] = job.name
            it[status] = job.status.serialize()
            it[userOldModelPath] = job.userOldModelPath
            it[userNewModelName] = job.userNewModelName
            it[userDataset] = job.userDataset.serialize()
            it[userOptimizer] = job.userOptimizer.serialize()
            it[userLoss] = job.userLoss.serialize()
            it[userMetrics] = klaxon.toJsonString(job.userMetrics)
            it[userEpochs] = job.userEpochs
            it[generateDebugComments] = job.generateDebugComments
        }
    }

    fun count(): Int = transaction(database) {
        Jobs.selectAll().count()
    }

    fun fetch(limit: Int, offset: Int): List<Job> = transaction(database) {
        Jobs.selectAll()
            .limit(limit, offset)
            .map { Jobs.toDomain(it) }
    }

    fun getById(id: Int): Job? = transaction(database) {
        Jobs.select { Jobs.id eq id }
            .map { Jobs.toDomain(it) }
            .firstOrNull()
    }

    fun findByName(name: String): Job? = transaction(database) {
        Jobs.select { Jobs.name eq name }
            .map { Jobs.toDomain(it) }
            .firstOrNull()
    }

    fun remove(id: Int): Int? = transaction(database) {
        Jobs.deleteWhere { Jobs.id eq id }
    }
}
