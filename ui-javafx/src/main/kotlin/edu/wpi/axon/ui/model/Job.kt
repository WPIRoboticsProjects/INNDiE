package edu.wpi.axon.ui.model

import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.JobTrainingMethod
import edu.wpi.axon.plugin.Plugin
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel
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

    //    val userOptimizer = bind(Job::userOptimizer)
    //    val userLoss = bind(Job::userLoss)
    //    val userMetrics = bind(Job::userMetrics)

    val userEpochsProperty = SimpleIntegerProperty(job.userEpochs)
    var userEpochs by userEpochsProperty

    //    val userNewModel = bind(Job::userNewModel)
    //    val generateDebugComments = bind(Job::generateDebugComments)

    val trainingMethodProperty = SimpleObjectProperty<JobTrainingMethod>(job.trainingMethod)
    var trainingMethod by trainingMethodProperty

    //    val trainingMethod = bind(Job::trainingMethod)
    //    val target = bind(Job::target)
    val datasetPluginProperty = SimpleObjectProperty<Plugin>(job.datasetPlugin)
    var datasetPlugin by datasetPluginProperty

    val idProperty = SimpleIntegerProperty(job.id)
    var id by idProperty

//    fun getDbObject(): Job = job.copy(name, status, userDataset)

    override fun toString(): String {
        return "JobDto(nameProperty=$nameProperty, statusProperty=$statusProperty, userOldModelPathProperty=$userOldModelPathProperty, userDatasetProperty=$userDatasetProperty, userEpochsProperty=$userEpochsProperty, trainingMethodProperty=$trainingMethodProperty, datasetPluginProperty=$datasetPluginProperty, idProperty=$idProperty)"
    }
}

class JobModel : ItemViewModel<JobDto>() {
    val name = bind(JobDto::nameProperty)
    val status = bind(JobDto::statusProperty)
    val userOldModelPath = bind(JobDto::userOldModelPathProperty)
    val userDataset = bind(JobDto::userDatasetProperty)
    //    val userOptimizer = bind(Job::userOptimizer)
//    val userLoss = bind(Job::userLoss)
//    val userMetrics = bind(Job::userMetrics)
    val userEpochs = bind(JobDto::userEpochsProperty)
    //    val userNewModel = bind(Job::userNewModel)
//    val generateDebugComments = bind(Job::generateDebugComments)
    val trainingMethod = bind(JobDto::trainingMethodProperty)
    //    val target = bind(Job::target)
    val datasetPlugin = bind(JobDto::datasetPluginProperty)
    val id = bind(JobDto::idProperty)

//    init {
//        userDataset.addListener { obs, old, new ->
//            println(new)
//        }
//    }

    override fun onCommit() {
        println(item)
    }
}
