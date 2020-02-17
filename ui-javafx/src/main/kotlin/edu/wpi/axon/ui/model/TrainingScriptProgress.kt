package edu.wpi.axon.ui.model

import edu.wpi.axon.db.data.TrainingScriptProgress
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ProgressBar
import tornadofx.ItemViewModel

class TrainingScriptProgressModel : ItemViewModel<TrainingScriptProgress>() {
    val text = bind { SimpleStringProperty(item?.let { it::class.simpleName }) }
    val progress = bind { SimpleDoubleProperty(
        item.let {
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
    ) }
    val state = bind { SimpleObjectProperty(
            item.let {
                when (it) {
                    TrainingScriptProgress.NotStarted -> State.NORMAL
                    TrainingScriptProgress.Creating -> State.NORMAL
                    TrainingScriptProgress.Initializing -> State.NORMAL
                    is TrainingScriptProgress.InProgress -> State.NORMAL
                    TrainingScriptProgress.Completed -> State.SUCCESS
                    is TrainingScriptProgress.Error -> State.ERROR
                    else -> State.ERROR
                }
            }
        )
    }
}

enum class State {
    NORMAL, SUCCESS, ERROR
}
