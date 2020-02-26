package edu.wpi.axon.ui.view

import arrow.core.Option
import edu.wpi.axon.db.data.DesiredJobTrainingMethod
import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.ui.JobLifecycleManager
import edu.wpi.axon.ui.ModelManager
import edu.wpi.axon.ui.model.AdamDto
import edu.wpi.axon.ui.model.AdamModel
import edu.wpi.axon.ui.model.CoralDto
import edu.wpi.axon.ui.model.CoralModel
import edu.wpi.axon.ui.model.DatasetModel
import edu.wpi.axon.ui.model.DatasetType
import edu.wpi.axon.ui.model.FTRLDto
import edu.wpi.axon.ui.model.FTRLModel
import edu.wpi.axon.ui.model.JobModel
import edu.wpi.axon.ui.model.ModelSourceType
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.axonBucketName
import edu.wpi.axon.util.datasetPluginManagerName
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.util.StringConverter
import kotlin.reflect.KClass
import tornadofx.Fieldset
import tornadofx.Fragment
import tornadofx.ItemFragment
import tornadofx.ItemViewModel
import tornadofx.action
import tornadofx.bind
import tornadofx.bindTo
import tornadofx.booleanBinding
import tornadofx.borderpane
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.center
import tornadofx.checkbox
import tornadofx.chooseFile
import tornadofx.combobox
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.fieldset
import tornadofx.filterInput
import tornadofx.form
import tornadofx.hbox
import tornadofx.isDouble
import tornadofx.isInt
import tornadofx.label
import tornadofx.separator
import tornadofx.textfield
import tornadofx.toObservable
import tornadofx.tooltip
import tornadofx.validator
import tornadofx.vbox

class JobEditor : Fragment() {
    private val job by inject<JobModel>()
    private val jobLifecycleManager by di<JobLifecycleManager>()
    private val bucketName by di<Option<String>>(axonBucketName)

    override val root = borderpane {
        center {
            add<JobConfiguration>()
        }
        bottom = buttonbar {
            button("Revert") {
                enableWhen(job.dirty)
                setOnAction {
                    job.rollback()
                }
            }
            button("Save") {
                enableWhen(job.status.booleanBinding {
                    it == TrainingScriptProgress.NotStarted
                }.and(job.dirty))
                setOnAction {
                    job.commit()
                }
            }
            button("Run") {
                enableWhen(job.status.booleanBinding {
                    it == TrainingScriptProgress.NotStarted
                })

                val desiredTrainingMethod = SimpleObjectProperty(
                    bucketName.fold(
                        { DesiredJobTrainingMethod.LOCAL },
                        { DesiredJobTrainingMethod.EC2 }
                    )
                )

                combobox<DesiredJobTrainingMethod> {
                    bind(desiredTrainingMethod)
                    items = DesiredJobTrainingMethod.values().toList().toObservable()
                    cellFormat {
                        text = it.name.toLowerCase().capitalize()
                    }
                    setOnAction { it.consume() }
                }

                action {
                    job.commit {
                        jobLifecycleManager.startJob(
                            job.id.value.toInt(),
                            desiredTrainingMethod.value
                        )
                    }
                }
            }
            button("Cancel") {
                enableWhen(job.status.booleanBinding {
                    it == TrainingScriptProgress.Creating ||
                        it == TrainingScriptProgress.Initializing ||
                        it is TrainingScriptProgress.InProgress
                })

                action {
                    jobLifecycleManager.cancelJob(job.id.value.toInt())
                }
            }
        }
    }
}

fun <T : Any> KClass<T>.objectInstanceOrEmptyCtor(): T =
    objectInstance ?: constructors.first { it.parameters.isEmpty() }.call()

class JobConfiguration : Fragment("Configuration") {
    private val job by inject<JobModel>()
    private val datasetPluginManager by di<PluginManager>(datasetPluginManagerName)

    override val root = form {
        hbox(20) {
            vbox(20) {
                fieldset("Dataset") {
                    add<DatasetPicker>()
                    field("Plugin") {
                        tooltip(
                            """
                            The plugin used to process the dataset before giving it to the model for training.
                            Add new plugins in the plugin editor.
                            """.trimIndent()
                        )
                        combobox(job.datasetPlugin) {
                            items = datasetPluginManager.listPlugins().toList().toObservable()
                            cellFormat {
                                text = it.name.toLowerCase().capitalize()
                            }
                        }
                    }
                }
                separator()
                fieldset("Model") {
                    add<ModelPicker>()
                }
            }
            vbox(20) {
                fieldset {
                    field("Epochs") {
                        tooltip(
                            """
                            The number of iterations over the dataset preformed when training the model.
                            More epochs takes longer but usually produces a more accurate model.
                            """.trimIndent()
                        )
                        textfield(job.userEpochs) {
                            filterInput { it.controlNewText.isInt() }
                            validator { isNotNull(it) }
                        }
                    }
                }
                separator()
                fieldset("Optimizer") {
                    field("Type") {
                        combobox(job.optimizerType) {
                            tooltip(
                                """
                                The type of the optimizer to use when training the model.
                                Different optimizers are better for different models.
                                """.trimIndent()
                            )
                            items = Optimizer::class.sealedSubclasses.toObservable()
                            cellFormat {
                                text = it.simpleName ?: "UNKNOWN"
                            }
                            valueProperty().addListener { _, _, newValue ->
                                if (newValue != null) {
                                    // Make an empty optimizer of the new type for the
                                    // OptimizerFragment to edit
                                    job.userOptimizer.value = newValue.objectInstanceOrEmptyCtor()
                                }
                            }
                        }
                    }
                    field {
                        button("Edit") {
                            action {
                                find<OptimizerFragment>().openModal(modality = Modality.WINDOW_MODAL)
                            }
                        }
                    }
                }
                separator()
                fieldset("Loss") {
                    field("Type") {
                        combobox(job.lossType) {
                            tooltip(
                                """
                                The type of the loss function to use.
                                Different loss functions are better for different models and tasks.
                                """.trimIndent()
                            )
                            items = Loss::class.sealedSubclasses.toObservable()
                            cellFormat {
                                text = it.simpleName ?: "UNKNOWN"
                            }
                            valueProperty().addListener { _, _, newValue ->
                                if (newValue != null) {
                                    // Make an empty optimizer of the new type for the
                                    // LossFragment to edit
                                    job.userLoss.value = newValue.objectInstanceOrEmptyCtor()
                                }
                            }
                        }
                    }
                    field {
                        button("Edit") {
                            action {
                                find<LossFragment>().openModal(modality = Modality.WINDOW_MODAL)
                            }
                        }
                    }
                }
            }
            vbox(20) {
                fieldset("Target") {
                    field("Type") {
                        combobox(job.targetType) {
                            tooltip(
                                """
                                The target machine that the model will run on.
                                """.trimIndent()
                            )
                            items = ModelDeploymentTarget::class.sealedSubclasses.toObservable()
                            cellFormat {
                                text = it.simpleName ?: "UNKNOWN"
                            }
                            valueProperty().addListener { _, _, newValue ->
                                if (newValue != null) {
                                    job.target.value = newValue.objectInstanceOrEmptyCtor()
                                }
                            }
                        }
                    }
                    field {
                        button("Edit") {
                            action {
                                find<TargetFragment>().openModal(modality = Modality.WINDOW_MODAL)
                            }
                        }
                    }
                }
            }
        }
    }
}

class DatasetPicker : ItemFragment<Dataset>() {
    private val job by inject<JobModel>()
    private val dataset = DatasetModel().bindTo(this)

    override val root = vbox {
        field("Type") {
            combobox(dataset.type) {
                items = DatasetType.values().toList().toObservable()
                cellFormat {
                    text = it.name.toLowerCase().capitalize()
                }
            }
        }
        field("Selection") {
            contentMap(dataset.type) {
                item(DatasetType.EXAMPLE) {
                    combobox(job.userDataset) {
                        tooltip(
                            """
                            Example datasets are simple, easy ways to test a model before curating a real dataset.
                            """.trimIndent()
                        )
                        items = Dataset.ExampleDataset::class.sealedSubclasses
                            .map { it.objectInstance }
                            .toObservable()
                        cellFormat {
                            text = it.displayName
                        }
                    }
                }
                item(DatasetType.CUSTOM) {
                    vbox {
                        button {
                            setOnAction {
                                val file = chooseFile(
                                    "Pick",
                                    arrayOf(FileChooser.ExtensionFilter("Any", "*.*"))
                                )
                                file.firstOrNull()?.let {
                                    job.userDataset.value =
                                        Dataset.Custom(FilePath.Local(it.path), it.name)
                                }
                            }
                        }
                        label(job.userDataset, converter = object : StringConverter<Dataset>() {
                            override fun toString(obj: Dataset?) = obj?.displayName ?: ""
                            override fun fromString(string: String) = null
                        })
                    }
                }
            }
        }
    }

    init {
        itemProperty.bind(job.userDataset)
    }
}

class ModelPicker : ItemFragment<ModelSource>() {

    private val job by inject<JobModel>()
    private val exampleModelManager by di<ExampleModelManager>()
    private val modelManager by di<ModelManager>()

    init {
        job.oldModelType.addListener { _, _, newValue ->
            val newOldModelType = when (newValue) {
                ModelSourceType.EXAMPLE -> if (job.userOldModelPath.value !is ModelSource.FromExample) null else job.userOldModelPath.value
                ModelSourceType.FILE -> if (job.userOldModelPath.value !is ModelSource.FromFile) null else job.userOldModelPath.value
                ModelSourceType.JOB -> if (job.userOldModelPath.value !is ModelSource.FromJob) null else job.userOldModelPath.value
                null -> null
            }

            job.userOldModelPath.value = newOldModelType
        }
    }

    override val root = vbox {
        field("Source") {
            combobox(job.oldModelType) {
                items = ModelSourceType.values().toList().toObservable()
                cellFormat {
                    text = it.name.toLowerCase().capitalize()
                }
            }
        }
        field {
            contentMap(job.oldModelType) {
                item(ModelSourceType.EXAMPLE) {
                    combobox(job.userOldModelPath) {
                        items = exampleModelManager.getAllExampleModels().unsafeRunSync().map {
                            ModelSource.FromExample(it)
                        }.toObservable()
                        cellFormat {
                            text = (it as? ModelSource.FromExample)?.exampleModel?.name ?: ""
                        }
                        valueProperty().addListener { _, _, newValue ->
                            if (newValue != null && job.isDirty) {
                                job.userNewModel.value = modelManager.loadModel(newValue)
                            }
                        }
                    }
                }
                item(ModelSourceType.FILE) {
                    vbox {
                        label(
                            job.userOldModelPath,
                            converter = object : StringConverter<ModelSource>() {
                                override fun toString(obj: ModelSource?) =
                                    (obj as? ModelSource.FromFile)?.filePath?.toString() ?: ""

                                override fun fromString(string: String?) = null
                            })
                    }
                }
                item(ModelSourceType.JOB) {
                    vbox {
                        label("Job")
                    }
                }
            }
        }
        field {
            button("Edit") {
                action {
                    find<LayerEditorFragment>().openModal(modality = Modality.WINDOW_MODAL)
                }
            }
        }
    }

    init {
        itemProperty.bind(job.userOldModelPath)
    }
}

class LayerEditorFragment : Fragment() {

    private val job by inject<JobModel>()

    override val root = borderpane {
        val layerEditor = LayerEditor(job.userNewModel.value)
        center = layerEditor

        bottom = buttonbar {
            button("Save") {
                action {
                    val newModel = layerEditor.getNewModel()
                    job.userNewModel.value = null
                    job.userNewModel.value = newModel
                    close()
                }
            }
            button("Cancel") {
                action {
                    close()
                }
            }
        }
    }
}

class OptimizerFragment : Fragment() {
    private val job by inject<JobModel>()
    lateinit var model: ItemViewModel<*>

    override val root = form {
        fieldset("Edit Optimizer") {
            println("Loaded with opt type: ${job.optimizerType.value}")
            println("Loaded with opt: ${job.userOptimizer.value}")

            require(job.optimizerType.value == job.userOptimizer.value::class)

            model = when (val opt = job.userOptimizer.value) {
                is Optimizer.Adam -> createAdamFields(opt)
                is Optimizer.FTRL -> createFTRLFields(opt)
            }
        }

        button("Save") {
            action {
                model.commit {
                    close()
                }
            }
        }
    }

    private fun Fieldset.createAdamFields(opt: Optimizer.Adam): ItemViewModel<*> {
        @Suppress("UNCHECKED_CAST")
        val adamModel = AdamModel(job.userOptimizer as Property<Optimizer.Adam>).apply {
            item = AdamDto(opt)
        }

        field("Learning Rate") {
            textfield(adamModel.learningRate) {
                filterInput { it.controlNewText.isDouble() }
            }
        }
        field("Beta 1") {
            textfield(adamModel.beta1) {
                filterInput { it.controlNewText.isDouble() }
            }
        }
        field("Beta 2") {
            textfield(adamModel.beta2) {
                filterInput { it.controlNewText.isDouble() }
            }
        }
        field("Epsilon") {
            textfield(adamModel.epsilon) {
                filterInput { it.controlNewText.isDouble() }
            }
        }
        field("AMS Grad") {
            checkbox(property = adamModel.amsGrad)
        }

        return adamModel
    }

    private fun Fieldset.createFTRLFields(opt: Optimizer.FTRL): ItemViewModel<*> {
        @Suppress("UNCHECKED_CAST")
        val ftrlModel = FTRLModel(job.userOptimizer as Property<Optimizer.FTRL>).apply {
            item = FTRLDto(opt)
        }

        field("Learning Rate") {
            textfield(ftrlModel.learningRate) {
                filterInput { it.controlNewText.isDouble() }
            }
        }
        field("Learning Rate Power") {
            textfield(ftrlModel.learningRatePower) {
                filterInput { it.controlNewText.isDouble() }
                validator {
                    isDoubleLessThanOrEqualToZero(it)
                }
            }
        }
        field("Initial Accumulator Value") {
            textfield(ftrlModel.initialAccumulatorValue) {
                filterInput { it.controlNewText.isDouble() }
                validator {
                    isDoubleGreaterThanOrEqualToZero(it)
                }
            }
        }
        field("L1 Regularization Strength") {
            textfield(ftrlModel.l1RegularizationStrength) {
                filterInput { it.controlNewText.isDouble() }
                validator {
                    isDoubleGreaterThanOrEqualToZero(it)
                }
            }
        }
        field("L2 Regularization Strength") {
            textfield(ftrlModel.l2RegularizationStrength) {
                filterInput { it.controlNewText.isDouble() }
                validator {
                    isDoubleGreaterThanOrEqualToZero(it)
                }
            }
        }
        field("L2 Shrinkage Regularization Strength") {
            textfield(ftrlModel.l2ShrinkageRegularizationStrength) {
                filterInput { it.controlNewText.isDouble() }
                validator {
                    isDoubleGreaterThanOrEqualToZero(it)
                }
            }
        }

        return ftrlModel
    }
}

class LossFragment : Fragment() {
    private val job by inject<JobModel>()
    lateinit var model: ItemViewModel<*>

    override val root = form {
        fieldset("Edit Loss") {
            println("Loaded with loss type: ${job.lossType.value}")
            println("Loaded with loss: ${job.userLoss.value}")
            model = when (val loss = job.userLoss.value) {
                is Loss.SparseCategoricalCrossentropy -> {
                    field {
                        label("SparseCategoricalCrossentropy has no data.")
                    }
                    object : ItemViewModel<Unit>() {}
                }

                is Loss.MeanSquaredError -> {
                    field {
                        label("MeanSquaredError has no data.")
                    }
                    object : ItemViewModel<Unit>() {}
                }
            }
        }

        button("Save") {
            action {
                model.commit {
                    close()
                }
            }
        }
    }
}

class TargetFragment : Fragment() {
    private val job by inject<JobModel>()
    lateinit var model: ItemViewModel<*>

    override val root = form {
        fieldset("Edit Loss") {
            println("Loaded with target type: ${job.targetType.value}")
            println("Loaded with target: ${job.target.value}")
            model = when (val target = job.target.value) {
                is ModelDeploymentTarget.Desktop -> {
                    field {
                        label("Desktop has no data.")
                    }
                    object : ItemViewModel<Unit>() {}
                }

                is ModelDeploymentTarget.Coral -> createCoralFields(target)
            }
        }

        button("Save") {
            action {
                model.commit {
                    close()
                }
            }
        }
    }

    private fun Fieldset.createCoralFields(target: ModelDeploymentTarget.Coral): ItemViewModel<*> {
        @Suppress("UNCHECKED_CAST")
        val coralModel = CoralModel(job.target as Property<ModelDeploymentTarget.Coral>).apply {
            item = CoralDto(target)
        }

        field("Representative Dataset Percentage") {
            textfield(
                coralModel.representativeDatasetPercentage,
                converter = object : StringConverter<Double>() {
                    override fun toString(obj: Double?) = obj?.let { it * 100 }?.toString()
                    override fun fromString(string: String?) =
                        string?.toDoubleOrNull()?.let { it / 100 }
                }) {
                filterInput { it.controlNewText.isDouble() }
                validator {
                    isDoubleInRange(it, 0.0..100.0)
                }
            }
        }

        return coralModel
    }
}
