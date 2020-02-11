package edu.wpi.axon.ui.model

import edu.wpi.axon.db.data.Job
import tornadofx.ItemViewModel

class JobModel : ItemViewModel<Job>() {
    val name = bind(Job::name)
    val status = bind(Job::status)
    val userOldModelPath = bind(Job::userOldModelPath)
    val userDataset = bind(Job::userDataset)
    val userOptimizer = bind(Job::userOptimizer)
    val userLoss = bind(Job::userLoss)
    val userMetrics = bind(Job::userMetrics)
    val userEpochs = bind(Job::userEpochs)
    val userNewModel = bind(Job::userNewModel)
    val generateDebugComments = bind(Job::generateDebugComments)
    val trainingMethod = bind(Job::trainingMethod)
    val target = bind(Job::target)
    val datasetPlugin = bind(Job::datasetPlugin)
    val id = bind(Job::id)
}

fun test() {
    var model = JobModel()

    model.userOptimizer.value
}
