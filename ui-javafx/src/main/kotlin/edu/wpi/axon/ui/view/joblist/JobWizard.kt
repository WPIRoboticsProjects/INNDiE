package edu.wpi.axon.ui.view.joblist

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.ui.controller.WizardTaskService
import edu.wpi.axon.ui.model.JobDto
import edu.wpi.axon.ui.model.JobWizardModel
import edu.wpi.axon.ui.model.TaskInput
import edu.wpi.axon.ui.view.isIntGreaterThanOrEqualTo
import javafx.geometry.Pos
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import tornadofx.ValidationMessage
import tornadofx.ValidationSeverity
import tornadofx.View
import tornadofx.Wizard
import tornadofx.bindSelected
import tornadofx.booleanBinding
import tornadofx.combobox
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
import tornadofx.required
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

    override val complete = job.valid(job.task)

    override val root = form {
        fieldset(title) {
            combobox(job.task) {
                tooltip(
                    """
                    The type of machine learning task.
                    """.trimIndent()
                )

                items = taskService.tasks

                cellFormat {
                    text = it.title
                }

                required()
            }
        }
    }
}

class InputSelection : View("Input") {
    val job: JobWizardModel by inject()

    override val complete = job.taskInput.booleanBinding { it != null }

    override val root = form {
        datagrid<TaskInput> {
            bindSelected(job.taskInput)

            cellWidth = 250.0
            cellHeight = 250.0

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

                    label(taskInput.title)
                    imageview(taskInput.graphic) {
                        isPreserveRatio = false
                        fitWidth = 150.0
                        fitHeight = 150.0
                    }
                    label(taskInput.description)
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
    val job: JobWizardModel by inject()

    override val complete = job.valid(job.targetType)

    override val root = form {
        fieldset(title) {
            combobox(job.targetType) {
                tooltip(
                    """
                    The target machine that the model will run on.
                    """.trimIndent()
                )

                items = ModelDeploymentTarget::class.sealedSubclasses.toObservable()

                cellFormat {
                    text = it.simpleName
                }

                required()
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
