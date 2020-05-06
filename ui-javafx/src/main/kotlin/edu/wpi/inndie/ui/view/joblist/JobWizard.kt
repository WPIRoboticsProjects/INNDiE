package edu.wpi.inndie.ui.view.joblist

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.inndie.ui.controller.WizardTaskService
import edu.wpi.inndie.ui.model.JobDto
import edu.wpi.inndie.ui.model.JobWizardModel
import edu.wpi.inndie.ui.model.TaskInput
import edu.wpi.inndie.ui.view.isIntGreaterThanOrEqualTo
import javafx.geometry.Pos
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.text.TextAlignment
import tornadofx.ValidationMessage
import tornadofx.ValidationSeverity
import tornadofx.View
import tornadofx.Wizard
import tornadofx.bindSelected
import tornadofx.booleanBinding
import tornadofx.datagrid
import tornadofx.field
import tornadofx.fieldset
import tornadofx.filterInput
import tornadofx.form
import tornadofx.imageview
import tornadofx.isInt
import tornadofx.label
import tornadofx.objectBinding
import tornadofx.observableListOf
import tornadofx.textfield
import tornadofx.toObservable
import tornadofx.tooltip
import tornadofx.validator
import tornadofx.vbox

class JobWizard : Wizard("Create job", "Provide job information") {
    val job: JobWizardModel by inject()

    override val canGoNext = currentPageComplete
    override val canFinish = allPagesComplete

    private val exampleModelManager by di<ExampleModelManager>()

    init {
        job.item = JobDto(null)

        add(TaskSelection::class)
        add(InputSelection::class)
        add(TrainingOptions::class)
        add(TargetSelection::class)
        add(FinalInformation::class)

        enableStepLinks = true

        // Refresh the example model cache when the user opens the wizard just in case it's out of
        // date
        runAsync {
            exampleModelManager.updateCache().unsafeRunSync()
        }
    }

    override fun onSave() {
        job.commit()
        super.onSave()
    }
}

class TaskSelection : View("Task") {
    private val taskService: WizardTaskService by inject()
    val job: JobWizardModel by inject()

    override val complete = job.task.booleanBinding { it != null }

    override val root = form {
        fieldset(title) {

            datagrid(taskService.tasks) {
                bindSelected(job.task)

                cellWidth = 200.0
                cellHeight = 200.0

                cellCache { task ->
                    vbox {
                        addEventFilter(MouseEvent.MOUSE_CLICKED) {
                            if (it.button == MouseButton.PRIMARY) {
                                selectionModel.select(task)
                            }
                        }

                        alignment = Pos.CENTER

                        label(task.title) {
                            alignment = Pos.TOP_CENTER
                        }
                        imageview(task.graphic) {
                            isPreserveRatio = false
                            fitWidth = 125.0
                            fitHeight = 125.0
                        }
                        label(task.description) {
                            isWrapText = true
                            alignment = Pos.BOTTOM_CENTER
                            textAlignment = TextAlignment.CENTER
                        }
                    }
                }
            }
        }
    }
}

class InputSelection : View("Input") {
    val job: JobWizardModel by inject()

    override val complete = job.taskInput.booleanBinding { it != null }

    override val root = form {
        fieldset(title) {
            datagrid<TaskInput> {
                bindSelected(job.taskInput)

                cellWidth = 200.0
                cellHeight = 200.0

                itemsProperty.bind(job.task.objectBinding {
                    it?.supportedInputs?.toObservable() ?: observableListOf()
                })
                cellCache { taskInput ->
                    vbox {
                        addEventFilter(MouseEvent.MOUSE_CLICKED) {
                            if (it.button == MouseButton.PRIMARY) {
                                selectionModel.select(taskInput)
                            }
                        }

                        alignment = Pos.CENTER

                        label(taskInput.title) {
                            alignment = Pos.TOP_CENTER
                        }
                        imageview(taskInput.graphic) {
                            isPreserveRatio = false
                            fitWidth = 125.0
                            fitHeight = 125.0
                        }
                        label(taskInput.description) {
                            isWrapText = true
                            alignment = Pos.BOTTOM_CENTER
                            textAlignment = TextAlignment.CENTER
                        }
                    }
                }
            }
        }
    }
}

class TrainingOptions : View("Training Options") {
    val job: JobWizardModel by inject()

    override val complete = job.valid(job.userEpochs)

    override val root = form {
        fieldset(title) {
            field("Epochs") {
                tooltip(
                    """
                    The number of iterations over the dataset preformed when training the model.
                    More epochs takes longer but usually produces a more accurate model.
                    """.trimIndent()
                )
                textfield(job.userEpochs) {
                    filterInput { it.controlNewText.isInt() }
                    validator { it.isIntGreaterThanOrEqualTo(1) }
                }
            }
        }
    }
}

class TargetSelection : View("Target") {
    private val taskService: WizardTaskService by inject()
    val job: JobWizardModel by inject()

    override val complete = job.wizardTarget.booleanBinding { it != null }

    override val root = form {
        fieldset(title) {
            datagrid(taskService.targets) {
                bindSelected(job.wizardTarget)

                cellWidth = 200.0
                cellHeight = 200.0

                cellCache { target ->
                    vbox {
                        addEventFilter(MouseEvent.MOUSE_CLICKED) {
                            if (it.button == MouseButton.PRIMARY) {
                                selectionModel.select(target)
                            }
                        }

                        alignment = Pos.CENTER

                        label(target.title) {
                            alignment = Pos.TOP_CENTER
                        }
                        imageview(target.graphic) {
                            isPreserveRatio = false
                            fitWidth = 125.0
                            fitHeight = 125.0
                        }
                        label(target.description) {
                            isWrapText = true
                            alignment = Pos.BOTTOM_CENTER
                            textAlignment = TextAlignment.CENTER
                        }
                    }
                }
            }
        }
    }
}

class FinalInformation : View("Finish") {
    private val database by di<JobDb>()

    val job: JobWizardModel by inject()

    override val complete = job.valid(job.name)

    override val root = form {
        fieldset(title) {
            field("Name") {
                textfield(job.name) {
                    validator {
                        if (it.isNullOrBlank()) {
                            ValidationMessage(
                                "Must not be empty.", ValidationSeverity.Error
                            )
                        } else {
                            if (database.findByName(it) == null) {
                                null
                            } else {
                                ValidationMessage(
                                    "A Job with that name already exists.",
                                    ValidationSeverity.Error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
