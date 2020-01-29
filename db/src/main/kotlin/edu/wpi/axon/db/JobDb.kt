package edu.wpi.axon.db

import com.beust.klaxon.Klaxon
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.util.FilePath
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
    val nameCol = varchar("name", 255).uniqueIndex()
    val statusCol = varchar("status", 255)
    val userOldModelPathCol = varchar("userOldModelPath", 255)
    val userNewModelNameCol = varchar("userNewModelName", 255)
    val userDatasetCol = text("dataset")
    val userOptimizerCol = varchar("userOptimizer", 255)
    val userLossCol = varchar("userLoss", 255)
    val userMetricsCol = varchar("userMetrics", 255)
    val userEpochsCol = integer("userEpochs")
    val userModelCol = text("userModel")
    val generateDebugCommentsCol = bool("generateDebugComments")

    fun toDomain(row: ResultRow) = Job(
        name = row[nameCol],
        status = TrainingScriptProgress.deserialize(row[statusCol]),
        userOldModelPath = FilePath.deserialize(row[userOldModelPathCol]),
        userNewModelName = FilePath.deserialize(row[userNewModelNameCol]),
        userDataset = Dataset.deserialize(row[userDatasetCol]),
        userOptimizer = Optimizer.deserialize(row[userOptimizerCol]),
        userLoss = Loss.deserialize(row[userLossCol]),
        userMetrics = klaxon.parseArray<String>(row[userMetricsCol])!!.toSet(),
        userEpochs = row[userEpochsCol],
        generateDebugComments = row[generateDebugCommentsCol],
        userNewModel = Model.deserialize(row[userModelCol]),
        id = row[id].value
    )
}

/**
 * The operations that you can do with a Job and the DB.
 */
sealed class JobDbOp {
    object Create : JobDbOp()
    object Update : JobDbOp()
    object Remove : JobDbOp()
}

typealias JobCallback = (JobDbOp, Job) -> Unit

class JobDb(private val database: Database) {

    private val observers = mutableListOf<JobCallback>()

    init {
        transaction(database) {
            SchemaUtils.create(Jobs)
        }
    }

    fun subscribe(onUpdate: JobCallback) {
        observers.add(onUpdate)
    }

    fun create(
        name: String,
        status: TrainingScriptProgress,
        userOldModelPath: FilePath,
        userNewModelName: FilePath,
        userDataset: Dataset,
        userOptimizer: Optimizer,
        userLoss: Loss,
        userMetrics: Set<String>,
        userEpochs: Int,
        userNewModel: Model,
        generateDebugComments: Boolean
    ): Job {
        val newId = transaction(database) {
            Jobs.insertAndGetId { row ->
                row[nameCol] = name
                row[statusCol] = status.serialize()
                row[userOldModelPathCol] = userOldModelPath.serialize()
                row[userNewModelNameCol] = userNewModelName.serialize()
                row[userDatasetCol] = userDataset.serialize()
                row[userOptimizerCol] = userOptimizer.serialize()
                row[userLossCol] = userLoss.serialize()
                row[userMetricsCol] = klaxon.toJsonString(userMetrics)
                row[userEpochsCol] = userEpochs
                row[userModelCol] = userNewModel.serialize()
                row[generateDebugCommentsCol] = generateDebugComments
            }.value
        }

        val job = Job(
            name = name,
            status = status,
            userOldModelPath = userOldModelPath,
            userNewModelName = userNewModelName,
            userDataset = userDataset,
            userOptimizer = userOptimizer,
            userLoss = userLoss,
            userMetrics = userMetrics,
            userEpochs = userEpochs,
            userNewModel = userNewModel,
            generateDebugComments = generateDebugComments,
            id = newId
        )

        observers.forEach { it(JobDbOp.Create, job) }

        return job
    }

    fun update(job: Job) = update(
        job.id,
        job.name,
        job.status,
        job.userOldModelPath,
        job.userNewModelName,
        job.userDataset,
        job.userOptimizer,
        job.userLoss,
        job.userMetrics,
        job.userEpochs,
        job.userNewModel,
        job.generateDebugComments
    )

    fun update(
        id: Int,
        name: String? = null,
        status: TrainingScriptProgress? = null,
        userOldModelPath: FilePath? = null,
        userNewModelName: FilePath? = null,
        userDataset: Dataset? = null,
        userOptimizer: Optimizer? = null,
        userLoss: Loss? = null,
        userMetrics: Set<String>? = null,
        userEpochs: Int? = null,
        userNewModel: Model? = null,
        generateDebugComments: Boolean? = null
    ) {
        transaction(database) {
            Jobs.update({ Jobs.id eq id }) { row ->
                name?.let { row[nameCol] = name }
                status?.let { row[statusCol] = status.serialize() }
                userOldModelPath?.let { row[userOldModelPathCol] = userOldModelPath.serialize() }
                userNewModelName?.let { row[userNewModelNameCol] = userNewModelName.serialize() }
                userDataset?.let { row[userDatasetCol] = userDataset.serialize() }
                userOptimizer?.let { row[userOptimizerCol] = userOptimizer.serialize() }
                userLoss?.let { row[userLossCol] = userLoss.serialize() }
                userMetrics?.let { row[userMetricsCol] = klaxon.toJsonString(userMetrics) }
                userEpochs?.let { row[userEpochsCol] = userEpochs }
                userNewModel?.let { row[userModelCol] = userNewModel.serialize() }
                generateDebugComments?.let { row[generateDebugCommentsCol] = generateDebugComments }
            }
        }

        observers.forEach { it(JobDbOp.Update, getById(id)!!) }
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
        Jobs.select { Jobs.nameCol eq name }
            .map { Jobs.toDomain(it) }
            .firstOrNull()
    }

    fun fetchRunningJobs(): List<Job> = transaction(database) {
        Jobs.select {
            Jobs.statusCol notInList listOf(
                TrainingScriptProgress.NotStarted.serialize(),
                TrainingScriptProgress.Completed.serialize(),
                TrainingScriptProgress.Error.serialize()
            )
        }.map { Jobs.toDomain(it) }
    }

    fun remove(job: Job) {
        transaction(database) {
            Jobs.deleteWhere { Jobs.id eq job.id }
        }

        observers.forEach { it(JobDbOp.Remove, job) }
    }
}
