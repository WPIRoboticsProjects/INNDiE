package edu.wpi.axon.ui.view.jobs

import com.github.mvysny.karibudsl.v10.beanValidationBinder
import com.github.mvysny.karibudsl.v10.bind
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.checkBox
import com.github.mvysny.karibudsl.v10.comboBox
import com.github.mvysny.karibudsl.v10.div
import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.hr
import com.github.mvysny.karibudsl.v10.label
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.toDouble
import com.github.mvysny.karibudsl.v10.toInt
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.binder.Result
import com.vaadin.flow.data.binder.ValidationResult
import com.vaadin.flow.data.converter.Converter
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.JobTrainingMethod
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.examplemodel.ExampleModel
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.axon.examplemodel.downloadAndConfigureExampleModel
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.ui.validateNotEmpty
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.datasetPluginManagerName
import kotlin.reflect.KClass
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

data class JobData(
    var name: String? = null,
    var trainingDataset: Dataset? = null,
    var datasetPlugin: Plugin? = null,
    var exampleModel: ExampleModel? = null,
    var optimizer: Optimizer? = null,
    var loss: Loss? = null,
    var metrics: Set<String>? = null,
    var epochs: Int? = null
) {

    fun convertToJob(exampleModelManager: ExampleModelManager, jobDb: JobDb): Job {
        val (model, file) = downloadAndConfigureExampleModel(
            exampleModel!!,
            exampleModelManager
        ).unsafeRunSync()

        return jobDb.create(
            name!!,
            TrainingScriptProgress.NotStarted,
            FilePath.Local(file.path),
            trainingDataset!!,
            optimizer!!,
            loss!!,
            metrics!!,
            epochs!!,
            model,
            false,
            JobTrainingMethod.Untrained,
            ModelDeploymentTarget.Desktop,
            datasetPlugin!!
        )
    }
}

class JobCreatorDialog : Dialog(), KoinComponent {

    private val binder = beanValidationBinder<JobData>()
    private val exampleModelManager: ExampleModelManager by inject()
    private val datasetPluginManager by inject<PluginManager>(named(datasetPluginManagerName))
    private val jobDb by inject<JobDb>()
    private var makeOptimizer: () -> Optimizer? = { null }
    var newJobId: Int? = null
        private set

    init {
        exampleModelManager.updateCache().unsafeRunSync() // TODO: Handle errors
        isCloseOnEsc = false
        isCloseOnOutsideClick = false

        div {
            verticalLayout {
                formLayout {
                    verticalLayout {
                        label("Basic Information")
                        hr()

                        textField("Job Name") {
                            bind(binder).asRequired().withValidator { value, _ ->
                                value?.let {
                                    val notEmpty = value.isNotEmpty()
                                    val notInDb = jobDb.findByName(value) == null
                                    if (notEmpty && notInDb) {
                                        ValidationResult.ok()
                                    } else {
                                        if (!notEmpty) {
                                            ValidationResult.error("Must not be empty.")
                                        } else if (!notInDb) {
                                            ValidationResult.error("Must be unique.")
                                        } else {
                                            ValidationResult.error("Unknown error.")
                                        }
                                    }
                                } ?: ValidationResult.error("Must not be empty.")
                            }.bind(JobData::name)
                        }
                    }

                    verticalLayout {
                        label("Training Data")
                        hr()

                        comboBox<Dataset>("Training Dataset") {
                            setWidthFull()
                            setItems(Dataset.ExampleDataset::class.sealedSubclasses.mapNotNull {
                                it.objectInstance
                            })
                            setItemLabelGenerator { it.displayName }
                            bind(binder).asRequired().bind(JobData::trainingDataset)
                        }

                        comboBox<Plugin>("Dataset Plugin") {
                            setWidthFull()
                            setItems(datasetPluginManager.listPlugins())
                            setItemLabelGenerator { it.name }
                            bind(binder).asRequired().bind(JobData::datasetPlugin)
                        }
                    }

                    verticalLayout {
                        label("Model")
                        hr()

                        comboBox<ExampleModel>("Starting Model") {
                            val exampleModels =
                                exampleModelManager.getAllExampleModels().unsafeRunSync()
                            setWidthFull()
                            setItems(exampleModels)
                            setItemLabelGenerator { it.name }
                            bind(binder).asRequired().bind(JobData::exampleModel)
                        }
                    }

                    verticalLayout {
                        label("Training")
                        hr()

                        textField("Metrics") {
                            placeholder = "accuracy, loss"
                            bind(binder).withConverter(
                                Converter.from<String, Set<String>>(
                                    {
                                        if (it == null) {
                                            Result.ok(setOf())
                                        } else {
                                            Result.ok(it.split(Regex(",\\s*")).toSet())
                                        }
                                    },
                                    {
                                        it.joinToString()
                                    }
                                )
                            ).bind(JobData::metrics)
                        }

                        textField("Epochs") {
                            placeholder = "1"
                            bind(binder).asRequired()
                                .toInt()
                                .withValidator(validateNotEmpty())
                                .bind(JobData::epochs)
                        }
                    }

                    verticalLayout {
                        label("Loss")
                        hr()

                        comboBox<Loss>("Loss") {
                            setWidthFull()
                            setItems(Loss::class.sealedSubclasses.mapNotNull { it.objectInstance })
                            setItemLabelGenerator { it::class.simpleName }
                            bind(binder).asRequired().bind(JobData::loss)
                        }
                    }

                    verticalLayout {
                        label("Optimizer")
                        hr()

                        verticalLayout {
                            val comboBoxVL = this
                            var optContent: Component? = null
                            comboBox<KClass<out Optimizer>> {
                                setWidthFull()
                                setItems(Optimizer::class.sealedSubclasses)
                                setItemLabelGenerator { it.simpleName }

                                addValueChangeListener {
                                    it.value?.let {
                                        optContent?.let { comboBoxVL.remove(it) }
                                        optContent = buildOptimizerComponent(it)
                                        comboBoxVL.add(optContent)
                                    }
                                }

                                bind(binder).asRequired()
                                    .withValidator(validateNotEmpty())
                                    .withConverter(
                                        Converter.from<KClass<out Optimizer>, Optimizer>(
                                            {
                                                val opt = makeOptimizer()
                                                if (opt != null) {
                                                    Result.ok(opt)
                                                } else {
                                                    Result.error("Invalid optimizer configuration.")
                                                }
                                            },
                                            {
                                                it::class
                                            }
                                        )
                                    ).bind(JobData::optimizer)
                            }
                        }
                    }
                }

                horizontalLayout {
                    button("Confirm", Icon(VaadinIcon.CHECK_CIRCLE)) {
                        val jobData = JobData()
                        onLeftClick {
                            if (binder.validate().isOk && binder.writeBeanIfValid(jobData)) {
                                LOGGER.debug {
                                    """
                                    |$jobData
                                    """.trimMargin()
                                }
                                val job = jobData.convertToJob(exampleModelManager, jobDb)
                                newJobId = job.id
                                LOGGER.debug { "New job id: $newJobId" }
                                close()
                            }
                        }
                    }

                    button("Cancel") {
                        onLeftClick {
                            close()
                        }
                    }
                }
            }
        }
    }

    private fun buildOptimizerComponent(optimizer: KClass<out Optimizer>): Component =
        when (optimizer) {
            Optimizer.Adam::class -> verticalLayout {
                data class AdamData(
                    var learningRate: Double? = null,
                    var beta1: Double? = null,
                    var beta2: Double? = null,
                    var epsilon: Double? = null,
                    var amsGrad: Boolean? = null
                )

                val binder = beanValidationBinder<AdamData>()

                textField("Learning Rate") {
                    placeholder = "0.001"
                    value = "0.001"
                    bind(binder).asRequired()
                        .toDouble()
                        .withValidator(validateNotEmpty())
                        .bind(AdamData::learningRate)
                }

                textField("Beta 1") {
                    placeholder = "0.9"
                    value = "0.9"
                    bind(binder).asRequired()
                        .toDouble()
                        .withValidator(validateNotEmpty())
                        .bind(AdamData::beta1)
                }

                textField("Beta 2") {
                    placeholder = "0.999"
                    value = "0.999"
                    bind(binder).asRequired()
                        .toDouble()
                        .withValidator(validateNotEmpty())
                        .bind(AdamData::beta2)
                }

                textField("Epsilon") {
                    placeholder = "1E-7"
                    value = "1E-7"
                    bind(binder).asRequired()
                        .toDouble()
                        .withValidator(validateNotEmpty())
                        .bind(AdamData::epsilon)
                }

                checkBox("AMSGrad") {
                    bind(binder).bind(AdamData::amsGrad)
                }

                makeOptimizer = {
                    val adamData = AdamData()
                    if (binder.validate().isOk && binder.writeBeanIfValid(adamData)) {
                        Optimizer.Adam(
                            adamData.learningRate!!,
                            adamData.beta1!!,
                            adamData.beta2!!,
                            adamData.epsilon!!,
                            adamData.amsGrad!!
                        )
                    } else null
                }
            }

            else -> Label("Unknown optimizer.")
        }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
