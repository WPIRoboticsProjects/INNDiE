package edu.wpi.axon.ui.model

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.InternalJobTrainingMethod
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.axon.plugin.DatasetPlugins
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.ui.ModelManager
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
import tornadofx.toObservable

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

    val userNewModelFilenameProperty = SimpleObjectProperty(job.userNewModelFilename)
    var userNewModelFilename by userNewModelFilenameProperty

    val internalTrainingMethodProperty = SimpleObjectProperty(job.internalTrainingMethod)
    var internalTrainingMethod by internalTrainingMethodProperty

    val targetProperty = SimpleObjectProperty<ModelDeploymentTarget>(job.target)
    var target by targetProperty

    val targetTypeProperty = SimpleObjectProperty(job.target::class)
    var targetType by targetTypeProperty

    val datasetPluginProperty = SimpleObjectProperty<Plugin>(job.datasetPlugin)
    var datasetPlugin by datasetPluginProperty

    val idProperty = SimpleIntegerProperty(job.id)
    var id by idProperty

    override fun toString(): String {
        return "JobDto(job=$job, nameProperty=$nameProperty, statusProperty=$statusProperty, userOldModelPathProperty=$userOldModelPathProperty, oldModelTypeProperty=$oldModelTypeProperty, userDatasetProperty=$userDatasetProperty, userOptimizerProperty=$userOptimizerProperty, optimizerTypeProperty=$optimizerTypeProperty, userLossProperty=$userLossProperty, lossTypeProperty=$lossTypeProperty, userMetricsProperty=$userMetricsProperty, userEpochsProperty=$userEpochsProperty, userNewModelProperty=$userNewModelProperty, userNewModelFilenameProperty=$userNewModelFilenameProperty, internalTrainingMethodProperty=$internalTrainingMethodProperty, targetProperty=$targetProperty, targetTypeProperty=$targetTypeProperty, datasetPluginProperty=$datasetPluginProperty, idProperty=$idProperty)"
    }
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
    val userNewModelFilename = bind(JobDto::userNewModelFilenameProperty)
    val internalTrainingMethod = bind(JobDto::internalTrainingMethodProperty)
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
            userNewModelFilename = FilePath.Local(getOutputModelName(userOldModelPath.value.filename)),
            target = target.value,
            datasetPlugin = datasetPlugin.value
        )
    }

    override fun toString() = "JobModel($item)"
}

class JobWizardModel : ItemViewModel<JobDto>() {

    private val modelManager by di<ModelManager>()
    private val exampleModelManager by di<ExampleModelManager>()

    val name = bind(JobDto::nameProperty, autocommit = true)
    val task = bind(autocommit = true) { SimpleObjectProperty<WizardTask>() }
    val taskInput = bind(autocommit = true) { SimpleObjectProperty<WizardTask.TaskInput>() }
    val targetType = bind(JobDto::targetTypeProperty, autocommit = true)
    val userEpochs = bind(JobDto::userEpochsProperty, autocommit = true)

    override fun onCommit() {
        // Logic for detecting parameters goes here
        val exampleModelName = "${task.value.title} - ${taskInput.value.title}"
        val exampleModel = exampleModelManager.getAllExampleModels()
            .unsafeRunSync()
            .firstOrNull { it.name == exampleModelName }

        check(exampleModel != null) {
            "No example model was found with name `$exampleModelName`"
        }

        val modelSource = ModelSource.FromExample(exampleModel)

        item = JobDto(
            Job(
                name = name.value,
                status = TrainingScriptProgress.NotStarted,
                userOldModelPath = modelSource,
                userDataset = taskInput.value.dataset,
                userOptimizer = taskInput.value.optimizer,
                userEpochs = userEpochs.value.toInt(),
                userLoss = taskInput.value.loss,
                userMetrics = setOf("accuracy").toObservable(),
                userNewModel = modelManager.loadModel(modelSource),
                userNewModelFilename = getOutputModelName(modelSource.filename),
                internalTrainingMethod = InternalJobTrainingMethod.Untrained,
                target = when (targetType.value) {
                    ModelDeploymentTarget.Desktop::class -> ModelDeploymentTarget.Desktop
                    ModelDeploymentTarget.Coral::class -> ModelDeploymentTarget.Coral()
                    else -> error("Invalid target")
                },
                generateDebugComments = false,
                datasetPlugin = extractDatasetPlugin(taskInput.value.dataset, modelSource),
                id = -1
            )
        )
    }

    private fun extractDatasetPlugin(dataset: Dataset, model: ModelSource.FromExample) =
        when (dataset) {
            Dataset.ExampleDataset.BostonHousing -> TODO()
            Dataset.ExampleDataset.Cifar10 -> TODO()
            Dataset.ExampleDataset.Cifar100 -> TODO()
            Dataset.ExampleDataset.FashionMnist -> DatasetPlugins.processMnistTypePlugin
            Dataset.ExampleDataset.IMDB -> TODO()
            Dataset.ExampleDataset.Mnist -> DatasetPlugins.processMnistTypePlugin
            Dataset.ExampleDataset.Reuters -> TODO()
            Dataset.ExampleDataset.AutoMPG -> DatasetPlugins.datasetPassthroughPlugin
            is Dataset.Custom -> TODO()
        }
}
