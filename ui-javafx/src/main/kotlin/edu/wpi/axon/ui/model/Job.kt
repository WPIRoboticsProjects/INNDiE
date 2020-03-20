package edu.wpi.axon.ui.model

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.getOutputModelName
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleSetProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.Commit
import tornadofx.ItemViewModel
import tornadofx.asObservable
import tornadofx.getValue
import tornadofx.setValue

data class JobDto(val job: Job) {
    val nameProperty = SimpleStringProperty(job.name)
    var name by nameProperty

    val statusProperty = SimpleObjectProperty(job.status)
    var status by statusProperty

    val userOldModelPathProperty = SimpleObjectProperty(job.userOldModelPath)
    var userOldModelPath by userOldModelPathProperty

    val oldModelTypeProperty = SimpleObjectProperty(
        when (job.userOldModelPath) {
            is ModelSource.FromExample -> ModelSourceType.EXAMPLE
            is ModelSource.FromFile -> ModelSourceType.FILE
            is ModelSource.FromJob -> ModelSourceType.JOB
        }
    )
    var oldModelType by oldModelTypeProperty

    val userDatasetProperty = SimpleObjectProperty(job.userDataset)
    var userDataset by userDatasetProperty

    val userOptimizerProperty = SimpleObjectProperty(job.userOptimizer)
    var userOptimizer by userOptimizerProperty

    val optimizerTypeProperty = SimpleObjectProperty(job.userOptimizer::class)
    var optimizerType by optimizerTypeProperty

    val userLossProperty = SimpleObjectProperty(job.userLoss)
    var userLoss by userLossProperty

    val lossTypeProperty = SimpleObjectProperty(job.userLoss::class)
    var lossType by lossTypeProperty

    val userMetricsProperty = SimpleSetProperty(job.userMetrics.asObservable())
    var userMetrics by userMetricsProperty

    val userEpochsProperty = SimpleIntegerProperty(job.userEpochs)
    var userEpochs by userEpochsProperty

    val userNewModelProperty = SimpleObjectProperty(job.userNewModel)
    var userNewModel by userNewModelProperty

    val userNewModelPathProperty = SimpleObjectProperty(job.userNewModelPath)
    var userNewModelPath by userNewModelPathProperty

    val targetProperty = SimpleObjectProperty<ModelDeploymentTarget>(job.target)
    var target by targetProperty

    val targetTypeProperty = SimpleObjectProperty(job.target::class)
    var targetType by targetTypeProperty

    val datasetPluginProperty = SimpleObjectProperty<Plugin>(job.datasetPlugin)
    var datasetPlugin by datasetPluginProperty

    val idProperty = SimpleIntegerProperty(job.id)
    var id by idProperty
}

class JobModel : ItemViewModel<JobDto>() {
    private val jobDb by di<JobDb>()

    val name = bind(JobDto::nameProperty)
    val status = bind(JobDto::statusProperty)
    val userOldModelPath = bind(JobDto::userOldModelPathProperty)
    val oldModelType = bind(JobDto::oldModelTypeProperty)
    val userDataset = bind(JobDto::userDatasetProperty)
    val userOptimizer = bind(JobDto::userOptimizerProperty)
    val optimizerType = bind(JobDto::optimizerTypeProperty)
    val userLoss = bind(JobDto::userLossProperty)
    val lossType = bind(JobDto::lossTypeProperty)
    val userMetrics = bind(JobDto::userMetricsProperty)
    val userEpochs = bind(JobDto::userEpochsProperty)
    val userNewModel = bind(JobDto::userNewModelProperty)
    val userNewModelPath = bind(JobDto::userNewModelPathProperty)
    val target = bind(JobDto::targetProperty)
    val targetType = bind(JobDto::targetTypeProperty)
    val datasetPlugin = bind(JobDto::datasetPluginProperty)
    val id = bind(JobDto::idProperty)

    override fun onCommit(commits: List<Commit>) {
        super.onCommit(commits)
        jobDb.update(
            id.value.toInt(),
            name = name.value,
            status = status.value,
            userOldModelPath = userOldModelPath.value,
            userDataset = userDataset.value,
            userOptimizer = userOptimizer.value,
            userLoss = userLoss.value,
            userMetrics = userMetrics.value,
            userEpochs = userEpochs.value.toInt(),
            userNewModel = userNewModel.value,
            userNewModelPath = FilePath.Local(getOutputModelName(userOldModelPath.value.filename)),
            target = target.value,
            datasetPlugin = datasetPlugin.value
        )
    }

    override fun toString() = "JobModel($item)"
}
