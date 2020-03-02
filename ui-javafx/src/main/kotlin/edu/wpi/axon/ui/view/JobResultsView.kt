package edu.wpi.axon.ui.view

import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.ui.JobLifecycleManager
import edu.wpi.axon.ui.model.JobModel
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.layout.VBox
import tornadofx.Fragment
import tornadofx.borderpane
import tornadofx.bottom
import tornadofx.label
import tornadofx.listview
import tornadofx.objectBinding
import tornadofx.paddingAll
import tornadofx.textarea
import tornadofx.toObservable

class JobResultsView : Fragment() {

    private val job by inject<JobModel>()
    private val jobLifecycleManager by di<JobLifecycleManager>()

    override val root = borderpane {
        centerProperty().bind(job.status.objectBinding { progress ->
            when (progress) {
                TrainingScriptProgress.Completed -> VBox().apply {
                    spacing = 10.0
                    label("Job completed successfully.")

                    listview<String> {
                        itemsProperty().bind(
                            job.id.objectBinding {
                                if (it == null) {
                                    emptyList()
                                } else {
                                    jobLifecycleManager.listResults(it.toInt())
                                }.toObservable()
                            }
                        )
                        selectionModel.selectionMode = SelectionMode.SINGLE
                        isEditable = false
                        selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                            if (newValue == null) {
                                bottom = null
                            } else {
                                bottom {
                                    add(
                                        find<ResultFragment>(
                                            mapOf(
                                                ResultFragment::data to LazyResult(
                                                    newValue,
                                                    lazy {
                                                        jobLifecycleManager.getResult(
                                                            job.id.value.toInt(),
                                                            newValue
                                                        )
                                                    }
                                                )
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                is TrainingScriptProgress.Error -> VBox().apply {
                    spacing = 10.0
                    label("Job completed erroneously. Error Log:")
                    textarea {
                        text = progress.log
                        isEditable = false
                        isWrapText = true
                    }
                }

                TrainingScriptProgress.NotStarted -> Label("Job has not been started yet.")
                TrainingScriptProgress.Creating, TrainingScriptProgress.Initializing -> Label("Job is starting.")
                is TrainingScriptProgress.InProgress -> Label("Job has not finished training yet.")
                null -> Label("No Job selected.")
            }.apply {
                paddingAll = 10
            }
        })
    }
}
