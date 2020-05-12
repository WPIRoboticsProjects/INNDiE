package edu.wpi.inndie.ui.view.joblist

import edu.wpi.inndie.db.data.TrainingScriptProgress
import edu.wpi.inndie.ui.model.JobDto
import edu.wpi.inndie.ui.model.JobModel
import javafx.geometry.Orientation
import tornadofx.ListCellFragment
import tornadofx.action
import tornadofx.bindTo
import tornadofx.booleanBinding
import tornadofx.contextmenu
import tornadofx.doubleBinding
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.item
import tornadofx.label
import tornadofx.onChange
import tornadofx.progressbar
import tornadofx.stringBinding
import tornadofx.textarea
import tornadofx.vbox

class JobListFragment : ListCellFragment<JobDto>() {
    private val job = JobModel().bindTo(this)

    override val root = vbox {
        label(job.name.stringBinding(job.id) { "$it (${job.id.value})" })
        progressbar(doubleBinding(job.status) {
            value.let {
                when (it) {
                    TrainingScriptProgress.NotStarted -> 0.0
                    TrainingScriptProgress.Creating -> javafx.scene.control.ProgressBar.INDETERMINATE_PROGRESS
                    TrainingScriptProgress.Initializing -> javafx.scene.control.ProgressBar.INDETERMINATE_PROGRESS
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
