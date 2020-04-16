package edu.wpi.axon.ui.view.joblist

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.ui.model.JobWizardModel
import edu.wpi.axon.ui.model.WizardTask
import edu.wpi.axon.ui.view.isIntGreaterThanOrEqualTo
import tornadofx.ValidationMessage
import tornadofx.ValidationSeverity
import tornadofx.View
import tornadofx.Wizard
import tornadofx.combobox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.filterInput
import tornadofx.form
import tornadofx.isInt
import tornadofx.objectBinding
import tornadofx.observableListOf
import tornadofx.onChange
import tornadofx.required
import tornadofx.textfield
import tornadofx.toObservable
import tornadofx.tooltip
import tornadofx.validator


class JobWizard : Wizard("Create job", "Provide job information") {
    val job: JobWizardModel by inject()

    override val canGoNext = currentPageComplete
    override val canFinish = allPagesComplete

    init {
        add(TaskSelection::class)
        add(InputSelection::class)
        add(TrainingOptions::class)
        add(TargetSelection::class)
        add(FinalInformation::class)

        enableStepLinks = true
    }

    override fun onSave() {
        job.commit()
    }
}

class TaskSelection : View("Task") {
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

                items = WizardTask::class.sealedSubclasses.map { it.objectInstance } .toList().toObservable()

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

    override val complete = job.valid(job.taskInput)

    override val root = form {
        fieldset(title) {
            combobox(job.taskInput) {
                tooltip(
                    """
                    The input to the machine learning process.
                    """.trimIndent()
                )

                job.task.objectBinding { it?.supportedInputs?.toObservable() ?: observableListOf() }.onChange { items = it }

                cellFormat {
                    text = it.title
                }

                required()
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
            textfield(job.name) {
                validator {
                    if (it.isNullOrBlank()) {
                        ValidationMessage("Must not be empty.", ValidationSeverity.Error
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