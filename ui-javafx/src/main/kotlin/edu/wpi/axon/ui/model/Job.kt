package edu.wpi.axon.ui.model

import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.JobTrainingMethod
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.training.ModelDeploymentTarget
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleSetProperty
import javafx.beans.property.SimpleStringProperty
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

    val userDatasetProperty = SimpleObjectProperty(job.userDataset)
    var userDataset by userDatasetProperty

    val userOptimizerProperty = SimpleObjectProperty(job.userOptimizer)
    var userOptimizer by userOptimizerProperty

    val optimizerTypeProperty = SimpleObjectProperty(job.userOptimizer::class)
    var optimizerType by optimizerTypeProperty

    val userLossProperty = SimpleObjectProperty(job.userLoss)
    var userLoss by userLossProperty

    val userMetricsProperty = SimpleSetProperty(job.userMetrics.asObservable())
    var userMetrics by userMetricsProperty

    val userEpochsProperty = SimpleIntegerProperty(job.userEpochs)
    var userEpochs by userEpochsProperty

    val userNewModelProperty = SimpleObjectProperty(job.userNewModel)
    var userNewModel by userNewModelProperty

    val generateDebugCommentsProperty = SimpleBooleanProperty(job.generateDebugComments)
    var generateDebugComments by generateDebugCommentsProperty

    val trainingMethodProperty = SimpleObjectProperty<JobTrainingMethod>(job.trainingMethod)
    var trainingMethod by trainingMethodProperty

    val targetProperty = SimpleObjectProperty<ModelDeploymentTarget>(job.target)
    var target by targetProperty

    val datasetPluginProperty = SimpleObjectProperty<Plugin>(job.datasetPlugin)
    var datasetPlugin by datasetPluginProperty

    val idProperty = SimpleIntegerProperty(job.id)
    var id by idProperty

    override fun toString(): String {
        return "JobDto(nameProperty=$nameProperty, statusProperty=$statusProperty, userOldModelPathProperty=$userOldModelPathProperty, userDatasetProperty=$userDatasetProperty, userOptimizerProperty=$userOptimizerProperty, userLossProperty=$userLossProperty, userMetricsProperty=$userMetricsProperty, userEpochsProperty=$userEpochsProperty, userNewModelProperty=$userNewModelProperty, generateDebugCommentsProperty=$generateDebugCommentsProperty, trainingMethodProperty=$trainingMethodProperty, targetProperty=$targetProperty, datasetPluginProperty=$datasetPluginProperty, idProperty=$idProperty)"
    }
}

class JobModel : ItemViewModel<JobDto>() {
    val name = bind(JobDto::nameProperty)
    val status = bind(JobDto::statusProperty)
    val userOldModelPath = bind(JobDto::userOldModelPathProperty)
    val userDataset = bind(JobDto::userDatasetProperty)
    val userOptimizer = bind(JobDto::userOptimizerProperty)
    val optimizerType = bind(JobDto::optimizerTypeProperty)
    val userLoss = bind(JobDto::userLossProperty)
    val userMetrics = bind(JobDto::userMetricsProperty)
    val userEpochs = bind(JobDto::userEpochsProperty)
    val userNewModel = bind(JobDto::userNewModelProperty)
    val generateDebugComments = bind(JobDto::generateDebugCommentsProperty)
    val trainingMethod = bind(JobDto::trainingMethodProperty)
    val target = bind(JobDto::targetProperty)
    val datasetPlugin = bind(JobDto::datasetPluginProperty)
    val id = bind(JobDto::idProperty)

    override fun onCommit() {
        println(item)
    }
}
