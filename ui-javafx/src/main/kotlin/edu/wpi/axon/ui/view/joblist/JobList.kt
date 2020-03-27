package edu.wpi.axon.ui.view.joblist

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.InternalJobTrainingMethod
import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.axon.plugin.DatasetPlugins.processMnistTypePlugin
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.ui.ModelManager
import edu.wpi.axon.ui.controller.JobBoard
import edu.wpi.axon.ui.model.JobModel
import edu.wpi.axon.util.getOutputModelName
import javafx.collections.ListChangeListener
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.ValidationMessage
import tornadofx.ValidationSeverity
import tornadofx.View
import tornadofx.action
import tornadofx.bindSelected
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.insets
import tornadofx.listview
import tornadofx.textfield
import tornadofx.validator
import tornadofx.vbox
import tornadofx.vgrow

class JobList : View() {

    private val jobBoard by inject<JobBoard>()
    private val job by inject<JobModel>()
    private val database by di<JobDb>()
    private val exampleModelManager by di<ExampleModelManager>()
    private val modelManager by di<ModelManager>()

    override val root = vbox {
        padding = insets(5)

        listview(jobBoard.jobs) {
            vgrow = Priority.ALWAYS
            bindSelected(job)
            cellFragment(JobListFragment::class)

            // Select a Job when it is added
            jobBoard.jobs.addListener(ListChangeListener {
                if (it.next()) {
                    it.addedSubList.lastOrNull()?.let { selectionModel.select(it) }
                }
            })
        }

        buttonbar {
            button(graphic = FontAwesomeIconView(FontAwesomeIcon.MINUS)) {
                enableWhen(job.empty.not())
                setOnAction {
                    database.removeById(job.item.id)
                }
            }

            button(graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS)) {
                setOnAction {
                    dialog("Create New Job", labelPosition = Orientation.VERTICAL) {
                        val tempJob = JobModel()
                        field("Job Name") {
                            textfield(tempJob.name) {
                                validator {
                                    if (it.isNullOrBlank()) {
                                        ValidationMessage(
                                            "Must not be empty.",
                                            ValidationSeverity.Error
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
                                action {
                                    if (tempJob.isValid) {
                                        createNewJob(tempJob.name.value)
                                        close()
                                    }
                                }
                            }
                        }

                        buttonbar {
                            button("Save") {
                                enableWhen(tempJob.valid)
                                action {
                                    if (tempJob.isValid) {
                                        createNewJob(tempJob.name.value)
                                        close()
                                    }
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
            }
        }
    }

    private fun createNewJob(name: String) {
        val modelSource = ModelSource.FromExample(
            exampleModelManager.getAllExampleModels()
                .unsafeRunSync()
                .first()
        )

        database.create(
            name = name,
            status = TrainingScriptProgress.NotStarted,
            userOldModelPath = modelSource,
            userDataset = Dataset.ExampleDataset.FashionMnist,
            userOptimizer = Optimizer.Adam(),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 1,
            userNewModel = modelManager.loadModel(modelSource),
            userNewModelFilename = getOutputModelName(modelSource.filename),
            generateDebugComments = false,
            internalTrainingMethod = InternalJobTrainingMethod.Untrained,
            target = ModelDeploymentTarget.Desktop,
            datasetPlugin = processMnistTypePlugin
        )
    }
}
