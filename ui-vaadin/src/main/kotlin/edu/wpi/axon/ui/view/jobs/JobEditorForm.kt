package edu.wpi.axon.ui.view.jobs

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
import kotlin.concurrent.thread
import org.koin.core.KoinComponent
import org.koin.core.inject

@StyleSheet("styles/job-form.css")
class JobEditorForm : KComposite(), KoinComponent {
    private val jobDb by inject<JobDb>()

    private val binder = beanValidationBinder<Job>()

    var job: Job? = null
        set(value) {
            field = value
            isVisible = value != null
            value?.let {
                binder.readBean(value)
            }
        }

    private val root = ui {
        div {
            className = "job-form"
            verticalLayout {
                className = "job-form-content"
                setSizeUndefined()
                formLayout {
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
                            setItems(Dataset.ExampleDataset::class.sealedSubclasses.mapNotNull { it.objectInstance })
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
                            val job = job!!
                            if (binder.validate().isOk && binder.writeBeanIfValid(job)) {
                                jobDb.update(job)
                                JobsView.navigateTo()
                            }
                        }
                    }
                    button("Delete", Icon(VaadinIcon.TRASH)) {
                        addThemeVariants(ButtonVariant.LUMO_ERROR)
                        isIconAfterText = true
                        setWidthFull()
                        onLeftClick {
                            jobDb.remove(job!!)
                            JobsView.navigateTo()
                        }
                    }
                    button("Run", Icon(VaadinIcon.PLAY)) {
                        isIconAfterText = true
                        setWidthFull()
                        onLeftClick {
                            thread(isDaemon = true) {
                                val jobRunner = JobRunner()
                                jobRunner.startJob(job!!).flatMap { id ->
                                    jobDb.update(job!!.copy(status = TrainingScriptProgress.Creating))
                                    jobRunner.waitForChange(id).flatMap {
                                        jobRunner.waitForCompleted(id) {
                                            jobDb.update(job!!.copy(status = it))
                                        }
                                    }
                                }.unsafeRunSync() // TODO: Handle errors here
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
}

@VaadinDsl
fun (@VaadinDsl HasComponents).jobEditorForm(block: (@VaadinDsl JobEditorForm).() -> Unit = {}): JobEditorForm =
    init(JobEditorForm(), block)
