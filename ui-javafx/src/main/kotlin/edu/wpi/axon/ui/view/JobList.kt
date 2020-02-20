package edu.wpi.axon.ui.view

import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.ui.controller.JobBoard
import edu.wpi.axon.ui.model.JobDto
import edu.wpi.axon.ui.model.JobModel
import javafx.scene.control.ProgressBar
import tornadofx.ListCellFragment
import tornadofx.View
import tornadofx.bindSelected
import tornadofx.bindTo
import tornadofx.doubleBinding
import tornadofx.label
import tornadofx.listview
import tornadofx.onChange
import tornadofx.progressbar
import tornadofx.vbox

class JobList : View() {
    private val jobBoard by inject<JobBoard>()
    private val job by inject<JobModel>()

    override val root = listview(jobBoard.jobs) {
        bindSelected(job)

        cellFragment(JobListFragment::class)
    }
}

class JobListFragment: ListCellFragment<JobDto>() {
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
                styleClass.setAll(when (it) {
                    TrainingScriptProgress.NotStarted -> listOf("progress-bar")
                    TrainingScriptProgress.Creating -> listOf("progress-bar")
                    TrainingScriptProgress.Initializing -> listOf("progress-bar")
                    is TrainingScriptProgress.InProgress -> listOf("progress-bar")
                    TrainingScriptProgress.Completed -> listOf("progress-bar-success", "progress-bar")
                    is TrainingScriptProgress.Error -> listOf("progress-bar-error", "progress-bar")
                    else -> listOf("progress-bar-error", "progress-bar")
                })
            }
        }
    }
}