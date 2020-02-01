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
import com.github.mvysny.karibudsl.v10.checkBox
import com.github.mvysny.karibudsl.v10.comboBox
import com.github.mvysny.karibudsl.v10.div
import com.github.mvysny.karibudsl.v10.formItem
import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.init
import com.github.mvysny.karibudsl.v10.numberField
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.setPrimary
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.toDouble
import com.github.mvysny.karibudsl.v10.toInt
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.ItemLabelGenerator
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.listbox.ListBox
import com.vaadin.flow.data.binder.Result
import com.vaadin.flow.data.binder.ValidationResult
import com.vaadin.flow.data.converter.Converter
import com.vaadin.flow.data.renderer.TextRenderer
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.ui.JobLifecycleManager
import edu.wpi.axon.util.axonBucketName
import kotlin.reflect.KClass
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named

@StyleSheet("styles/job-form.css")
class JobEditorForm : KComposite(), KoinComponent {

    private val jobDb by inject<JobDb>()
    private val jobLifecycleManager by inject<JobLifecycleManager>()
    private lateinit var form: FormLayout
    private val binder = beanValidationBinder<Job>()
    private var coralRepresentativeDatasetPercentage = 0.0

    var job: Option<Job> = None
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
                                bind(binder).asRequired().bind(Job::name)
                            }
                        }
                        formItem("Dataset") {
                            comboBox<Dataset> {
                                setWidthFull()
                                setItems(Dataset.ExampleDataset::class.sealedSubclasses.mapNotNull {
                                    it.objectInstance
                                })
                                setItemLabelGenerator { it.displayName }
                                bind(binder).asRequired().bind(Job::userDataset)
                            }
                        }
                        formItem("Epochs") {
                            numberField {
                                setWidthFull()
                                setHasControls(true)
                                min = 1.0
                                step = 1.0
                                bind(binder).toInt().asRequired().bind(Job::userEpochs)
                            }
                        }
                        formItem("Generate Debug Comments") {
                            checkBox {
                                bind(binder).bind(Job::generateDebugComments)
                            }
                        }
                        formItem("Target", configureTargetForm())
                    }
                    verticalLayout {
                        button("Save", Icon(VaadinIcon.CHECK), configureSaveButton())
                        button("Delete", Icon(VaadinIcon.TRASH), configureDeleteButton())
                        button("Run", Icon(VaadinIcon.PLAY), configureRunButton())
                        button("Cancel", Icon(VaadinIcon.STOP), configureCancelButton())
                    }
                }
            }
        }

        isVisible = false
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
            job.map {
                if (binder.validate().isOk && binder.writeBeanIfValid(it)) {
                    jobDb.update(it)
                    JobsView.navigateTo()
                }
            }
        }
    }

    private fun configureDeleteButton(): (@VaadinDsl Button).() -> Unit = {
        addThemeVariants(ButtonVariant.LUMO_ERROR)
        isIconAfterText = true
        setWidthFull()
        onLeftClick {
            job.map { job ->
                // TODO: Handle errors cancelling the Job
                IO {
                    jobLifecycleManager.cancelJob(job.id)
                }.map {
                    // Only remove the Job if it was successfully cancelled
                    jobDb.remove(job)
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
                        it.status != TrainingScriptProgress.Error
                }
            )
        }
        onLeftClick {
            job.map { job ->
                // TODO: Handle errors cancelling the Job
                IO {
                    jobLifecycleManager.cancelJob(job.id)
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
                    IO {
                        jobLifecycleManager.startJob(it)
                    }.unsafeRunSync()
                }
            )
        }
    }

    private fun configureTargetForm(): (@VaadinDsl FormLayout.FormItem).() -> Unit = {
        verticalLayout {
            val coralForm = FormLayout().apply {
                isVisible = false
                formItem("Representative Dataset Percentage") {
                    textField {
                        bind(binder).asRequired()
                            .toDouble()
                            .withValidator { value, _ ->
                                when (value) {
                                    null -> ValidationResult.error("Outside range.")
                                    in 0.0..1.0 -> ValidationResult.ok()
                                    else -> ValidationResult.error("Outside range.")
                                }
                            }
                            .bind(
                                { coralRepresentativeDatasetPercentage },
                                { _, value ->
                                    value?.let { coralRepresentativeDatasetPercentage = it }
                                }
                            )
                    }
                }
            }

            val targetConverter = Converter.from<
                KClass<out ModelDeploymentTarget>,
                ModelDeploymentTarget>(
                {
                    when (it) {
                        ModelDeploymentTarget.Desktop::class -> Result.ok(
                            ModelDeploymentTarget.Desktop
                        )

                        ModelDeploymentTarget.Coral::class -> Result.ok(
                            ModelDeploymentTarget.Coral(coralRepresentativeDatasetPercentage)
                        )

                        else -> Result.error<ModelDeploymentTarget>("Unknown selection.")
                    }
                },
                { it::class }
            )

            add(ListBox<KClass<out ModelDeploymentTarget>>().apply {
                setItems(
                    ModelDeploymentTarget.Desktop::class,
                    ModelDeploymentTarget.Coral::class
                )

                value = ModelDeploymentTarget.Desktop::class

                setRenderer(TextRenderer(ItemLabelGenerator { it.simpleName }))

                bind(binder)
                    .withConverter(targetConverter)
                    .bind(Job::target)

                addValueChangeListener {
                    coralForm.isVisible = it.value == ModelDeploymentTarget.Coral::class
                }
            })

            add(coralForm)
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
