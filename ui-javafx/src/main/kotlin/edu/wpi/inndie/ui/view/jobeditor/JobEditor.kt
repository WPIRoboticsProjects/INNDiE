package edu.wpi.inndie.ui.view.jobeditor

import arrow.core.Option
import edu.wpi.axon.db.data.DesiredJobTrainingMethod
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.ui.model.JobModel
import edu.wpi.inndie.ui.JobLifecycleManager
import edu.wpi.inndie.util.axonBucketName
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ButtonBar
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.Pane
import tornadofx.Fragment
import tornadofx.action
import tornadofx.bind
import tornadofx.booleanBinding
import tornadofx.borderpane
import tornadofx.button
import tornadofx.combobox
import tornadofx.enableWhen
import tornadofx.objectBinding
import tornadofx.toObservable

class JobEditor : Fragment() {

    private val job by inject<JobModel>()
    private val jobLifecycleManager by di<_root_ide_package_.edu.wpi.inndie.ui.JobLifecycleManager>()
    private val bucketName by di<Option<String>>(axonBucketName)

    override val root = borderpane {
        centerProperty().bind(job.itemProperty.objectBinding {
            if (it == null) {
                Label("No selection.")
            } else {
                ScrollPane().apply {
                    add<JobConfiguration>()
                }
            }
        })

        bottomProperty().bind(job.itemProperty.objectBinding {
            if (it == null) {
                Pane()
            } else {
                ButtonBar().apply {
                    button("Revert") {
                        enableWhen(job.dirty)
                        setOnAction {
                            job.rollback()
                        }
                    }
                    button("Save") {
                        enableWhen(job.status.booleanBinding {
                            it == TrainingScriptProgress.NotStarted
                        }.and(job.dirty))
                        setOnAction {
                            job.commit()
                        }
                    }
                    button("Run") {
                        enableWhen(job.status.booleanBinding {
                            it == TrainingScriptProgress.NotStarted
                        })

                        val desiredTrainingMethod = SimpleObjectProperty(
                            bucketName.fold(
                                { DesiredJobTrainingMethod.LOCAL },
                                { DesiredJobTrainingMethod.EC2 }
                            )
                        )

                        combobox<DesiredJobTrainingMethod> {
                            bind(desiredTrainingMethod)
                            items = DesiredJobTrainingMethod.values().toList().toObservable()
                            cellFormat {
                                text = it.name.toLowerCase().capitalize()
                            }
                            setOnAction { it.consume() }
                        }

                        action {
                            job.commit {
                                jobLifecycleManager.startJob(
                                    job.id.value.toInt(),
                                    desiredTrainingMethod.value
                                )
                            }
                        }
                    }
                    button("Cancel") {
                        enableWhen(job.status.booleanBinding {
                            it == TrainingScriptProgress.Creating ||
                                it == TrainingScriptProgress.Initializing ||
                                it is TrainingScriptProgress.InProgress
                        })

                        action {
                            jobLifecycleManager.cancelJob(job.id.value.toInt())
                        }
                    }
                }
            }
        })
    }
}
