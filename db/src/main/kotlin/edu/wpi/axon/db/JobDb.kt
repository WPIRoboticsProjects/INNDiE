package edu.wpi.axon.db

import com.beust.klaxon.Klaxon
import edu.wpi.axon.db.data.InternalJobTrainingMethod
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.ModelDeploymentTarget
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

private val klaxon = Klaxon()

internal object Jobs : IntIdTable() {

    val nameCol = varchar("name", 255).uniqueIndex()
    val statusCol = text("status")
    val userOldModelPathCol = text("userOldModelPath")
    val userDatasetCol = text("dataset")
    val userOptimizerCol = varchar("userOptimizer", 255)
    val userLossCol = varchar("userLoss", 255)
    val userMetricsCol = varchar("userMetrics", 255)
    val userEpochsCol = integer("userEpochs")
    val userModelCol = text("userModel")
    val generateDebugCommentsCol = bool("generateDebugComments")
    val internalTrainingMethodCol = varchar("internalTrainingMethod", 255)
    val targetCol = varchar("target", 255)
    val datasetPluginCol = text("datasetPlugin")

    fun toDomain(row: ResultRow) = Job(
        name = row[nameCol],
        status = TrainingScriptProgress.deserialize(row[statusCol]),
        userOldModelPath = ModelSource.deserialize(row[userOldModelPathCol]),
        userDataset = Dataset.deserialize(row[userDatasetCol]),
        userOptimizer = Optimizer.deserialize(row[userOptimizerCol]),
        userLoss = Loss.deserialize(row[userLossCol]),
        userMetrics = klaxon.parseArray<String>(row[userMetricsCol])!!.toSet(),
        userEpochs = row[userEpochsCol],
        generateDebugComments = row[generateDebugCommentsCol],
        userNewModel = Model.deserialize(row[userModelCol]),
        internalTrainingMethod = InternalJobTrainingMethod.deserialize(
            row[internalTrainingMethodCol]
        ),
        target = ModelDeploymentTarget.deserialize(row[targetCol]),
        datasetPlugin = Plugin.deserialize(row[datasetPluginCol]),
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
        userOldModelPath: ModelSource,
        userDataset: Dataset,
        userOptimizer: Optimizer,
        userLoss: Loss,
        userMetrics: Set<String>,
        userEpochs: Int,
        userNewModel: Model,
        generateDebugComments: Boolean,
        internalTrainingMethod: InternalJobTrainingMethod,
        target: ModelDeploymentTarget,
        datasetPlugin: Plugin
    ): Job {
        val newId = transaction(database) {
            Jobs.insertAndGetId { row ->
                row[nameCol] = name
                row[statusCol] = status.serialize()
                row[userOldModelPathCol] = userOldModelPath.serialize()
                row[userDatasetCol] = userDataset.serialize()
                row[userOptimizerCol] = userOptimizer.serialize()
                row[userLossCol] = userLoss.serialize()
                row[userMetricsCol] = klaxon.toJsonString(userMetrics)
                row[userEpochsCol] = userEpochs
                row[userModelCol] = userNewModel.serialize()
                row[generateDebugCommentsCol] = generateDebugComments
                row[internalTrainingMethodCol] = internalTrainingMethod.serialize()
                row[targetCol] = target.serialize()
                row[datasetPluginCol] = datasetPlugin.serialize()
            }.value
        }

        val job = Job(
            name = name,
            status = status,
            userOldModelPath = userOldModelPath,
            userDataset = userDataset,
            userOptimizer = userOptimizer,
            userLoss = userLoss,
            userMetrics = userMetrics,
            userEpochs = userEpochs,
            userNewModel = userNewModel,
            generateDebugComments = generateDebugComments,
            internalTrainingMethod = internalTrainingMethod,
            target = target,
            datasetPlugin = datasetPlugin,
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
        job.userDataset,
        job.userOptimizer,
        job.userLoss,
        job.userMetrics,
        job.userEpochs,
        job.userNewModel,
        job.generateDebugComments,
        job.internalTrainingMethod,
        job.target,
        job.datasetPlugin
    )

    fun update(
        id: Int,
        name: String? = null,
        status: TrainingScriptProgress? = null,
        userOldModelPath: ModelSource? = null,
        userDataset: Dataset? = null,
        userOptimizer: Optimizer? = null,
        userLoss: Loss? = null,
        userMetrics: Set<String>? = null,
        userEpochs: Int? = null,
        userNewModel: Model? = null,
        generateDebugComments: Boolean? = null,
        internalJobTrainingMethod: InternalJobTrainingMethod? = null,
        target: ModelDeploymentTarget? = null,
        datasetPlugin: Plugin? = null
    ): Job {
        transaction(database) {
            Jobs.update({ Jobs.id eq id }) { row ->
                name?.let { row[nameCol] = name }
                status?.let { row[statusCol] = status.serialize() }
                userOldModelPath?.let { row[userOldModelPathCol] = userOldModelPath.serialize() }
                userDataset?.let { row[userDatasetCol] = userDataset.serialize() }
                userOptimizer?.let { row[userOptimizerCol] = userOptimizer.serialize() }
                userLoss?.let { row[userLossCol] = userLoss.serialize() }
                userMetrics?.let { row[userMetricsCol] = klaxon.toJsonString(userMetrics) }
                userEpochs?.let { row[userEpochsCol] = userEpochs }
                userNewModel?.let { row[userModelCol] = userNewModel.serialize() }
                generateDebugComments?.let { row[generateDebugCommentsCol] = generateDebugComments }
                internalJobTrainingMethod?.let {
                    row[internalTrainingMethodCol] = internalJobTrainingMethod.serialize()
                }
                target?.let { row[targetCol] = target.serialize() }
                datasetPlugin?.let { row[datasetPluginCol] = datasetPlugin.serialize() }
            }
        }

        val newJob = getById(id)!!
        observers.forEach { it(JobDbOp.Update, newJob) }
        return newJob
    }

    fun count(): Int = transaction(database) {
        Jobs.selectAll().count()
    }

    fun fetchAll(): List<Job> = transaction(database) {
        Jobs.selectAll().map { Jobs.toDomain(it) }
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
        // TODO: Split the error log off of the status so we don't need to do this filter
        Jobs.select {
            (Jobs.statusCol like "%Creating%") or
                (Jobs.statusCol like "%Initializing%") or
                (Jobs.statusCol like "%InProgress%")
        }.map { Jobs.toDomain(it) }
            .filter { it.status !is TrainingScriptProgress.Error }
    }

    fun removeById(id: Int) {
        val job = getById(id)!!

        transaction(database) {
            Jobs.deleteWhere { Jobs.id eq id }
        }

        observers.forEach { it(JobDbOp.Remove, job) }
    }

    fun remove(job: Job) {
        transaction(database) {
            Jobs.deleteWhere { Jobs.id eq job.id }
        }

        observers.forEach { it(JobDbOp.Remove, job) }
    }
}
