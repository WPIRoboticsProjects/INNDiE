package edu.wpi.inndie.ui.model

import edu.wpi.inndie.db.JobDb
import edu.wpi.inndie.db.data.InternalJobTrainingMethod
import edu.wpi.inndie.db.data.Job
import edu.wpi.inndie.db.data.ModelSource
import edu.wpi.inndie.db.data.TrainingScriptProgress
import edu.wpi.inndie.examplemodel.ExampleModelManager
import edu.wpi.inndie.plugin.DatasetPlugins
import edu.wpi.inndie.plugin.Plugin
import edu.wpi.inndie.tfdata.Dataset
import edu.wpi.inndie.tfdata.Model
import edu.wpi.inndie.tfdata.loss.Loss
import edu.wpi.inndie.tfdata.optimizer.Optimizer
import edu.wpi.inndie.training.ModelDeploymentTarget
import edu.wpi.inndie.ui.ModelManager
import edu.wpi.inndie.util.FilePath
import edu.wpi.inndie.util.getOutputModelName
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleSetProperty
import javafx.beans.property.SimpleStringProperty
import kotlin.reflect.KClass
import tornadofx.Commit
import tornadofx.ItemViewModel
import tornadofx.asObservable
import tornadofx.getValue
import tornadofx.setValue
import tornadofx.toObservable

data class JobDto(val job: Job?) {
    val nameProperty = SimpleStringProperty(job?.name)
    var name by nameProperty

    val statusProperty = SimpleObjectProperty<TrainingScriptProgress>(job?.status)
    var status by statusProperty

    val userOldModelPathProperty = SimpleObjectProperty<ModelSource>(job?.userOldModelPath)
    var userOldModelPath by userOldModelPathProperty

    val oldModelTypeProperty = SimpleObjectProperty<ModelSourceType>(
        when (job?.userOldModelPath) {
            is ModelSource.FromExample, null -> ModelSourceType.EXAMPLE
            is ModelSource.FromFile -> ModelSourceType.FILE
        }
    )
    var oldModelType by oldModelTypeProperty

    val userDatasetProperty = SimpleObjectProperty<Dataset>(job?.userDataset)
    var userDataset by userDatasetProperty

    val userOptimizerProperty = SimpleObjectProperty<Optimizer>(job?.userOptimizer)
    var userOptimizer by userOptimizerProperty

    val optimizerTypeProperty =
        SimpleObjectProperty<KClass<out Optimizer>>(job?.let { it.userOptimizer::class })
    var optimizerType by optimizerTypeProperty

    val userLossProperty = SimpleObjectProperty<Loss>(job?.userLoss)
    var userLoss by userLossProperty

    val lossTypeProperty = SimpleObjectProperty<KClass<out Loss>>(job?.let { it.userLoss::class })
    var lossType by lossTypeProperty

    val userMetricsProperty = SimpleSetProperty<String>(job?.userMetrics?.asObservable())
    var userMetrics by userMetricsProperty

    val userEpochsProperty = SimpleIntegerProperty(job?.userEpochs ?: 1)
    var userEpochs by userEpochsProperty

    val userNewModelProperty = SimpleObjectProperty<Model>(job?.userNewModel)
    var userNewModel by userNewModelProperty

    val userNewModelFilenameProperty = SimpleObjectProperty<String>(job?.userNewModelFilename)
    var userNewModelFilename by userNewModelFilenameProperty

    val internalTrainingMethodProperty =
        SimpleObjectProperty<InternalJobTrainingMethod>(job?.internalTrainingMethod)
    var internalTrainingMethod by internalTrainingMethodProperty

    val targetProperty = SimpleObjectProperty<ModelDeploymentTarget>(job?.target)
    var target by targetProperty

    val targetTypeProperty =
        SimpleObjectProperty<KClass<out ModelDeploymentTarget>>(job?.let { it.target::class })
    var targetType by targetTypeProperty

    val datasetPluginProperty = SimpleObjectProperty<Plugin>(job?.datasetPlugin)
    var datasetPlugin by datasetPluginProperty

    val idProperty = SimpleIntegerProperty(job?.id ?: -1)
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
            userNewModelFilename = FilePath.Local(
                getOutputModelName(userOldModelPath.value.filename)
            ),
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
    val status = bind(JobDto::statusProperty, autocommit = true)
    val userOldModelPath = bind(JobDto::userOldModelPathProperty, autocommit = true)
    val oldModelType = bind(JobDto::oldModelTypeProperty, autocommit = true)
    val userDataset = bind(JobDto::userDatasetProperty, autocommit = true)
    val userOptimizer = bind(JobDto::userOptimizerProperty, autocommit = true)
    val optimizerType = bind(JobDto::optimizerTypeProperty, autocommit = true)
    val userLoss = bind(JobDto::userLossProperty, autocommit = true)
    val lossType = bind(JobDto::lossTypeProperty, autocommit = true)
    val userMetrics = bind(JobDto::userMetricsProperty, autocommit = true)
    val userEpochs = bind(JobDto::userEpochsProperty, autocommit = true)
    val userNewModel = bind(JobDto::userNewModelProperty, autocommit = true)
    val userNewModelFilename = bind(JobDto::userNewModelFilenameProperty, autocommit = true)
    val internalTrainingMethod = bind(JobDto::internalTrainingMethodProperty, autocommit = true)
    val target = bind(JobDto::targetProperty, autocommit = true)
    val targetType = bind(JobDto::targetTypeProperty, autocommit = true)
    val datasetPlugin = bind(JobDto::datasetPluginProperty, autocommit = true)

    val task = bind(autocommit = true) { SimpleObjectProperty<WizardTask>() }
    val taskInput = bind(autocommit = true) { SimpleObjectProperty<TaskInput>() }
    val wizardTarget = bind(autocommit = true) { SimpleObjectProperty<WizardTarget>() }

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

        status.value = TrainingScriptProgress.NotStarted
        userOldModelPath.value = modelSource
        oldModelType.value = ModelSourceType.EXAMPLE
        userDataset.value = taskInput.value.dataset
        userOptimizer.value = taskInput.value.optimizer
        optimizerType.value = taskInput.value.optimizer::class
        userLoss.value = taskInput.value.loss
        lossType.value = taskInput.value.loss::class
        userMetrics.value = setOf("accuracy").toObservable()
        userNewModel.value = modelManager.loadModel(modelSource)
        userNewModelFilename.value =
            getOutputModelName(modelSource.filename)
        internalTrainingMethod.value = InternalJobTrainingMethod.Untrained
        targetType.value = wizardTarget.value.targetClass
        target.value = when (targetType.value) {
            ModelDeploymentTarget.Desktop::class -> ModelDeploymentTarget.Desktop
            ModelDeploymentTarget.Coral::class -> ModelDeploymentTarget.Coral()
            else -> error("Invalid target")
        }
        datasetPlugin.value = extractDatasetPlugin(taskInput.value.dataset, modelSource)
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
