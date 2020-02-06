package edu.wpi.axon.ui.view.jobs

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.fx.IO
import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.beanValidationBinder
import com.github.mvysny.karibudsl.v10.bind
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.comboBox
import com.github.mvysny.karibudsl.v10.div
import com.github.mvysny.karibudsl.v10.formItem
import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.init
import com.github.mvysny.karibudsl.v10.numberField
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.setPrimary
import com.github.mvysny.karibudsl.v10.tabs
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.toInt
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Result
import com.vaadin.flow.data.binder.ValidationResult
import com.vaadin.flow.data.converter.Converter
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.JobTrainingMethod
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.ui.JobLifecycleManager
import edu.wpi.axon.ui.validateNotEmpty
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.allS3OrLocal
import edu.wpi.axon.util.axonBucketName
import edu.wpi.axon.util.datasetPluginManagerName
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.reflect.KClass
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named

enum class UIModelDeploymentTarget {
    Desktop, Coral
}

class UIJob(
    var name: String? = null,
    var status: TrainingScriptProgress? = null,
    var userOldModelPath: FilePath? = null,
    var userDataset: Dataset? = null,
    var userOptimizer: Optimizer? = null,
    var userLoss: Loss? = null,
    var userMetrics: Set<String>? = null,
    var userEpochs: Int? = null,
    var userNewModel: Model? = null,
    var trainingMethod: JobTrainingMethod? = null,
    var target: UIModelDeploymentTarget? = null,
    var coralRepresentativeDatasetPercentage: Double? = null,
    var datasetPlugin: Plugin? = null,
    var id: Int? = null
) {

    /**
     * Whether the Job uses AWS for anything. Is [None] if the Job configuration is incorrect
     * (mixing AWS and local). Is [Some] if the configuration is correct.
     */
    val usesAWS: Option<Boolean>
        get() {
            if (userOldModelPath == null || userOldModelPath == null) {
                return None
            }

            val s3Check = when (val dataset = userDataset) {
                null -> return None
                is Dataset.ExampleDataset -> allS3OrLocal(userOldModelPath!!)
                is Dataset.Custom -> allS3OrLocal(userOldModelPath!!, dataset.path)
            }

            return if (s3Check) {
                // Just need to check one because of the check above
                Some(userOldModelPath is FilePath.S3)
            } else {
                // If the check failed then the configuration is wrong
                None
            }
        }

    fun updateInDb(jobDb: JobDb) = jobDb.update(
        id = id!!,
        name = name!!,
        status = status!!,
        userOldModelPath = userOldModelPath!!,
        userDataset = userDataset!!,
        userOptimizer = userOptimizer!!,
        userLoss = userLoss!!,
        userMetrics = userMetrics!!,
        userEpochs = userEpochs!!,
        userNewModel = userNewModel!!,
        generateDebugComments = false,
        trainingMethod = trainingMethod!!,
        target = when (target!!) {
            UIModelDeploymentTarget.Desktop -> ModelDeploymentTarget.Desktop
            UIModelDeploymentTarget.Coral -> ModelDeploymentTarget.Coral(
                coralRepresentativeDatasetPercentage!!
            )
        },
        datasetPlugin = datasetPlugin!!
    )

    companion object {

        fun fromJob(job: Job) = UIJob(
            name = job.name,
            status = job.status,
            userOldModelPath = job.userOldModelPath,
            userDataset = job.userDataset,
            userOptimizer = job.userOptimizer,
            userLoss = job.userLoss,
            userMetrics = job.userMetrics,
            userEpochs = job.userEpochs,
            userNewModel = job.userNewModel,
            trainingMethod = job.trainingMethod,
            target = when (job.target) {
                is ModelDeploymentTarget.Desktop -> UIModelDeploymentTarget.Desktop
                is ModelDeploymentTarget.Coral -> UIModelDeploymentTarget.Coral
            },
            coralRepresentativeDatasetPercentage = when (val target = job.target) {
                is ModelDeploymentTarget.Desktop -> 0.0
                is ModelDeploymentTarget.Coral -> target.representativeDatasetPercentage
            },
            datasetPlugin = job.datasetPlugin,
            id = job.id
        )
    }
}

@StyleSheet("styles/job-form.css")
class JobEditorForm : KComposite(), KoinComponent {

    private val jobDb by inject<JobDb>()
    private val jobLifecycleManager by inject<JobLifecycleManager>()
    private lateinit var form: FormLayout
    private val binder = beanValidationBinder<UIJob>()
    private val datasetPluginManager by inject<PluginManager>(named(datasetPluginManagerName))
    private lateinit var tabs: Tabs
    private val tabMap = mutableMapOf<Tab, Component>()

    var job: Option<UIJob> = None
        set(value) {
            field = value
            isVisible = value is Some
            form.isEnabled =
                value.fold({ false }, { it.status == TrainingScriptProgress.NotStarted })
            value.map { binder.readBean(it) }
        }

    init {
        ui {
            div {
                className = "job-form"

                tabs = tabs {
                    val basicTab = Tab("Basic")
                    val basicPage = VerticalLayout().apply {
                        isVisible = false
                        configureBasicPage()
                    }

                    val inputTab = Tab("Input")
                    val inputPage = VerticalLayout().apply {
                        isVisible = false
                        configureInputPage()
                    }

                    val targetTab = Tab("Target")
                    val targetPage = VerticalLayout().apply {
                        isVisible = false
                        configureTargetPage()
                    }

                    // Add all the tabs and pages to the map to associate them
                    tabMap[basicTab] = basicPage
                    tabMap[inputTab] = inputPage
                    tabMap[targetTab] = targetPage

                    // Add the tabs to the tab bar
                    tabMap.forEach { (tab, _) -> add(tab) }

                    // Select and make visible the basic page (the first page)
                    selectedTab = basicTab
                    basicPage.isVisible = true

                    // Each time the user changes their selection, change which page is visible
                    addSelectedChangeListener {
                        tabMap.forEach { (_, page) -> page.isVisible = false }
                        it.selectedTab?.let { tabMap[it]!!.isVisible = true }
                    }
                }

                div { tabMap.forEach { (_, page) -> add(page) } }
            }
        }

        isVisible = false
    }

    @VaadinDsl
    private fun (@VaadinDsl HasComponents).configureBasicPage() {
        verticalLayout {
            className = "job-form-content"
            setSizeUndefined()
            form = formLayout {
                responsiveSteps = listOf(
                    FormLayout.ResponsiveStep(
                        "0",
                        1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP
                    ),
                    FormLayout.ResponsiveStep(
                        "550px",
                        1,
                        FormLayout.ResponsiveStep.LabelsPosition.ASIDE
                    )
                )
                formItem("Name") {
                    textField {
                        setWidthFull()
                        bind(binder).asRequired().bind(UIJob::name)
                    }
                }
                formItem("Epochs") {
                    numberField {
                        setWidthFull()
                        setHasControls(true)
                        min = 1.0
                        step = 1.0
                        bind(binder).toInt().asRequired().bind(UIJob::userEpochs)
                    }
                }
            }
            verticalLayout {
                button("Save", Icon(VaadinIcon.CHECK), configureSaveButton())
                button("Delete", Icon(VaadinIcon.TRASH), configureDeleteButton())
                button("Run", Icon(VaadinIcon.PLAY), configureRunButton())
                button("Cancel", Icon(VaadinIcon.STOP), configureCancelButton())
            }
        }
    }

    @VaadinDsl
    private fun (@VaadinDsl HasComponents).configureInputPage() {
        formLayout {
            formItem("Dataset") {
                comboBox<Dataset> {
                    setWidthFull()
                    setItems(Dataset.ExampleDataset::class.sealedSubclasses.mapNotNull {
                        it.objectInstance
                    })
                    setItemLabelGenerator { it.displayName }
                    bind(binder).asRequired().bind(UIJob::userDataset)
                }
            }
            formItem("Dataset Plugin") {
                comboBox<Plugin> {
                    setWidthFull()
                    setItems(datasetPluginManager.listPlugins())
                    setItemLabelGenerator { it.name }
                    bind(binder).asRequired().bind(UIJob::datasetPlugin)
                }
            }
        }
    }

    @VaadinDsl
    private fun (@VaadinDsl HasComponents).configureTargetPage() {
        formLayout {
            lateinit var percentageField: TextField

            formItem("Target") {
                comboBox<UIModelDeploymentTarget> {
                    setItems(UIModelDeploymentTarget.values().toList())
                    value = UIModelDeploymentTarget.Desktop
                    setItemLabelGenerator { it.name }
                    addValueChangeListener {
                        percentageField.isEnabled = it.value == UIModelDeploymentTarget.Coral
                    }
                    bind(binder)
                        .asRequired()
                        .withValidator(validateNotEmpty())
                        .bind(UIJob::target)
                }
            }

            formItem("Representative Dataset Percentage") {
                percentageField = textField {
                    placeholder = "1.0"
                    isEnabled = false

                    bind(binder)
                        .asRequired()
                        .withValidator(validateNotEmpty())
                        .withConverter(Converter.from<String, Double>(
                            {
                                try {
                                    Result.ok(it.toDouble() / 100)
                                } catch (ex: NumberFormatException) {
                                    Result.error<Double>(ex.localizedMessage)
                                }
                            },
                            {
                                DecimalFormat("#.####").apply {
                                    roundingMode = RoundingMode.HALF_DOWN
                                }.format(it * 100)
                            }
                        ))
                        .withValidator { value, _ ->
                            if (value in 0.0..100.0) ValidationResult.ok()
                            else ValidationResult.error("Value outside range [0, 100]: $value")
                        }
                        .bind(UIJob::coralRepresentativeDatasetPercentage)
                }
            }
        }
    }

    private fun configureSaveButton(): (@VaadinDsl Button).() -> Unit = {
        setPrimary()
        addThemeVariants(ButtonVariant.LUMO_SUCCESS)
        isIconAfterText = true
        setWidthFull()
        binder.addStatusChangeListener {
            isEnabled = binder.hasChanges() && !it.hasValidationErrors()
        }
        onLeftClick {
            saveJob()
        }
    }

    private fun saveJob() = job.map {
        if (binder.validate().isOk && binder.writeBeanIfValid(it)) {
            it.updateInDb(jobDb)
            JobsView.navigateTo()
        }
        it
    }

    private fun configureDeleteButton(): (@VaadinDsl Button).() -> Unit = {
        addThemeVariants(ButtonVariant.LUMO_ERROR)
        isIconAfterText = true
        setWidthFull()
        onLeftClick {
            job.map { job ->
                // TODO: Handle errors cancelling the Job
                IO {
                    jobLifecycleManager.cancelJob(job.id!!)
                }.map {
                    // Only remove the Job if it was successfully cancelled
                    jobDb.removeById(job.id!!)
                    JobsView.navigateTo()
                }
            }
        }
    }

    private fun configureCancelButton(): (@VaadinDsl Button).() -> Unit = {
        addThemeVariants(ButtonVariant.LUMO_ERROR)
        setWidthFull()
        binder.addStatusChangeListener {
            isEnabled = job.fold(
                {
                    // Nothing to cancel if there is no Job bound
                    false
                },
                {
                    // Nothing to cancel if the Job is not running
                    it.status != TrainingScriptProgress.NotStarted &&
                        it.status != TrainingScriptProgress.Completed &&
                        it.status !is TrainingScriptProgress.Error
                }
            )
        }
        onLeftClick {
            job.map { job ->
                // TODO: Handle errors cancelling the Job
                IO {
                    jobLifecycleManager.cancelJob(job.id!!)
                }.unsafeRunSync()
            }
        }
    }

    private fun configureRunButton(): (@VaadinDsl Button).() -> Unit = {
        isIconAfterText = true
        setWidthFull()
        binder.addStatusChangeListener {
            isEnabled = job.fold(
                {
                    // Nothing to run if there is no job bound
                    false
                },
                {
                    if (it.status != TrainingScriptProgress.NotStarted) {
                        // Don't let the user run jobs that are currently
                        // running
                        return@fold false
                    }

                    val bucket = get<Option<String>>(named(axonBucketName))

                    LOGGER.debug {
                        """
                        job=$it
                        usesAWS=${it.usesAWS}
                        bucket=$bucket
                        """.trimIndent()
                    }

                    it.usesAWS.fold(
                        {
                            // The Job is configured incorrectly, don't let
                            // it run
                            false
                        },
                        { jobUsesAWS ->
                            // The user can run the job if:
                            //  1. They have AWS configured and the Job uses AWS
                            //  2. They don't have AWS configured and the Job
                            //      doesn't use AWS
                            //  3. They have AWS configured and the Job doesn't
                            //      use AWS
                            bucket is Some || !jobUsesAWS
                        }
                    )
                }
            )
        }
        onLeftClick {
            LOGGER.debug { "Running $job" }
            job.fold(
                {
                    LOGGER.debug {
                        "Could not run the Job because it is None."
                    }
                },
                {
                    // TODO: Handle errors here
                    saveJob().map {
                        IO {
                            jobLifecycleManager.startJob(jobDb.getById(it.id!!)!!)
                        }.unsafeRunSync()
                    }
                }
            )
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).jobEditorForm(
    block: (@VaadinDsl JobEditorForm).() -> Unit = {}
): JobEditorForm = init(JobEditorForm(), block)
