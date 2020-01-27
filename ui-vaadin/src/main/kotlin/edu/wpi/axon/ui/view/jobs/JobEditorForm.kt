package edu.wpi.axon.ui.view.jobs

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.fx.IO
import arrow.fx.extensions.fx
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
import com.github.mvysny.karibudsl.v10.toInt
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.ui.JobRunner
import edu.wpi.axon.util.axonBucketName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named

@StyleSheet("styles/job-form.css")
class JobEditorForm : KComposite(), KoinComponent {

    private val jobDb by inject<JobDb>()
    private val jobRunner by inject<JobRunner>()
    private val scope = CoroutineScope(Dispatchers.Default)
    private lateinit var form: FormLayout
    private val binder = beanValidationBinder<Job>()

    var job: Option<Job> = None
        set(value) {
            field = value
            isVisible = value is Some
            form.isEnabled =
                value.fold({ false }, { it.status == TrainingScriptProgress.NotStarted })
            value.map { binder.readBean(it) }
        }

    private val root = ui {
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
                }
                verticalLayout {
                    button("Save", Icon(VaadinIcon.CHECK)) {
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
                    button("Delete", Icon(VaadinIcon.TRASH)) {
                        addThemeVariants(ButtonVariant.LUMO_ERROR)
                        isIconAfterText = true
                        setWidthFull()
                        onLeftClick {
                            job.map {
                                // TODO: Handle errors cancelling the Job
                                jobRunner.cancelJob(it.id).unsafeRunSync()
                                jobDb.remove(it)
                                JobsView.navigateTo()
                            }
                        }
                    }
                    button("Run", Icon(VaadinIcon.PLAY)) {
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
                                        // Don't let the user run jobs that are currently running
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
                                            // The Job is configured incorrectly, don't let it run
                                            false
                                        },
                                        { jobUsesAWS ->
                                            // The user can run the job if:
                                            //  1. They have AWS configured and the Job uses AWS
                                            //  2. They don't have AWS configured and the Job
                                            //      doesn't use AWS
                                            //  3. They have AWS configured and the Job doesn't
                                            //      use AWS
                                            (bucket is Some) || !jobUsesAWS
                                        }
                                    )
                                }
                            )
                        }
                        onLeftClick {
                            thread(isDaemon = true) {
                                job.fold(
                                    {
                                        LOGGER.debug { "Could not run the Job because it is None." }
                                    },
                                    { job ->
                                        scope.launch {
                                            runJob(job)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        isVisible = false
    }

    private suspend fun runJob(job: Job) = IO.fx {
        jobDb.update(job.copy(status = TrainingScriptProgress.Creating))

        jobRunner.startJob(job).bind()
        LOGGER.debug { "Started job with id: $id" }

        effect { delay(5000) }.bind()

        jobRunner.waitForFinish(job.id) {
            jobDb.update(job.copy(status = it))
        }.bind()
    }.unsafeRunSync() // TODO: Handle errors here

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).jobEditorForm(
    block: (@VaadinDsl JobEditorForm).() -> Unit = {}
): JobEditorForm = init(JobEditorForm(), block)
