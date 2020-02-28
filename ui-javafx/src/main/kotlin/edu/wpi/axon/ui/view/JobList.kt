package edu.wpi.axon.ui.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.InternalJobTrainingMethod
import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.axon.plugin.DatasetPlugins
import edu.wpi.axon.plugin.DatasetPlugins.processMnistTypePlugin
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.ui.ModelManager
import edu.wpi.axon.ui.controller.JobBoard
import edu.wpi.axon.ui.model.JobDto
import edu.wpi.axon.ui.model.JobModel
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.scene.control.ProgressBar
import org.koin.core.inject
import tornadofx.Fragment
import tornadofx.ListCellFragment
import tornadofx.View
import tornadofx.action
import tornadofx.bindSelected
import tornadofx.bindTo
import tornadofx.booleanBinding
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.contextmenu
import tornadofx.doubleBinding
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.form
import tornadofx.item
import tornadofx.label
import tornadofx.listview
import tornadofx.onChange
import tornadofx.progressbar
import tornadofx.textarea
import tornadofx.textfield
import tornadofx.vbox

class JobList : View() {

    private val jobBoard by inject<JobBoard>()
    private val job by inject<JobModel>()
    private val database by di<JobDb>()
    private val exampleModelManager by di<ExampleModelManager>()
    private val modelManager by di<ModelManager>()

    override val root = vbox {
        listview(jobBoard.jobs) {
            bindSelected(job)
            cellFragment(JobListFragment::class)
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
                        val nameProp = SimpleStringProperty()
                        field("Job Name") {
                            textfield(nameProp)
                        }

                        buttonbar {
                            button("Save") {
                                action {
                                    val modelSource = ModelSource.FromExample(
                                        exampleModelManager.getAllExampleModels()
                                            .unsafeRunSync()
                                            .first()
                                    )

                                    database.create(
                                        name = nameProp.value,
                                        status = TrainingScriptProgress.NotStarted,
                                        userOldModelPath = modelSource,
                                        userDataset = Dataset.ExampleDataset.FashionMnist,
                                        userOptimizer = Optimizer.Adam(),
                                        userLoss = Loss.SparseCategoricalCrossentropy,
                                        userMetrics = setOf("accuracy"),
                                        userEpochs = 1,
                                        userNewModel = modelManager.loadModel(modelSource),
                                        generateDebugComments = false,
                                        internalTrainingMethod = InternalJobTrainingMethod.Untrained,
                                        target = ModelDeploymentTarget.Desktop,
                                        datasetPlugin = processMnistTypePlugin
                                    )

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
            }
        }
    }
}

class JobListFragment : ListCellFragment<JobDto>() {
    private val job = JobModel().bindTo(this)

    override val root = vbox {
        label(job.name)
        progressbar(doubleBinding(job.status) {
            value.let {
                when (it) {
                    TrainingScriptProgress.NotStarted -> 0.0
                    TrainingScriptProgress.Creating -> ProgressBar.INDETERMINATE_PROGRESS
                    TrainingScriptProgress.Initializing -> ProgressBar.INDETERMINATE_PROGRESS
                    is TrainingScriptProgress.InProgress -> it.percentComplete
                    TrainingScriptProgress.Completed -> 1.0
                    is TrainingScriptProgress.Error -> 1.0
                    else -> 0.0
                }
            }
        }) {
            maxWidth = Double.MAX_VALUE

            job.status.onChange {
                styleClass.setAll(
                    when (it) {
                        TrainingScriptProgress.NotStarted -> listOf("progress-bar")
                        TrainingScriptProgress.Creating -> listOf("progress-bar")
                        TrainingScriptProgress.Initializing -> listOf("progress-bar")
                        is TrainingScriptProgress.InProgress -> listOf("progress-bar")
                        TrainingScriptProgress.Completed -> listOf(
                            "progress-bar-success",
                            "progress-bar"
                        )
                        is TrainingScriptProgress.Error -> listOf(
                            "progress-bar-error",
                            "progress-bar"
                        )
                        else -> listOf("progress-bar-error", "progress-bar")
                    }
                )
            }
        }
        contextmenu {
            item("View Error") {
                enableWhen(job.status.booleanBinding { it is TrainingScriptProgress.Error })
                action {
                    dialog(labelPosition = Orientation.VERTICAL) {
                        field("Error Log") {
                            textarea {
                                text = (job.status.value as TrainingScriptProgress.Error).log
                                isEditable = false
                                isWrapText = true
                            }
                        }
                    }
                }
            }
        }
    }
}
