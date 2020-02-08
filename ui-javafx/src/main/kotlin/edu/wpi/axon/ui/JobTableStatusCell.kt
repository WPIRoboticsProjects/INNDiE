package edu.wpi.axon.ui

import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.TrainingScriptProgress
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.TableCell
import javafx.scene.layout.VBox

class JobTableStatusCell : TableCell<Job, TrainingScriptProgress>() {

    override fun updateItem(status: TrainingScriptProgress?, empty: Boolean) {
        super.updateItem(status, empty)
        if (empty || status == null) {
            text = null
            graphic = null
        } else {
            alignment = Pos.CENTER_LEFT
            graphic = VBox().apply {
                alignment = Pos.CENTER_LEFT
                spacing = 2.0

                children.add(Label(status::class.simpleName))

                children.add(ProgressBar().apply {
                    maxWidth = Double.MAX_VALUE
                    progress = when (status) {
                        TrainingScriptProgress.NotStarted -> 0.0

                        TrainingScriptProgress.Creating -> ProgressBar.INDETERMINATE_PROGRESS

                        TrainingScriptProgress.Initializing -> ProgressBar.INDETERMINATE_PROGRESS

                        is TrainingScriptProgress.InProgress -> status.percentComplete

                        TrainingScriptProgress.Completed -> {
                            styleClass.setAll("progress-bar-success", "progress-bar")
                            1.0
                        }

                        is TrainingScriptProgress.Error -> {
                            styleClass.setAll("progress-bar-error", "progress-bar")
                            1.0
                        }
                    }
                })
            }
        }
    }
}
