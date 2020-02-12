package edu.wpi.axon.ui.model

import edu.wpi.axon.db.data.TrainingScriptProgress
import javafx.beans.property.ReadOnlyDoubleWrapper
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ProgressBar
import tornadofx.ItemViewModel
import tornadofx.asObservable
import tornadofx.getValue
import tornadofx.observable
import tornadofx.setValue

class TrainingScriptProgressDto(val status: TrainingScriptProgress) {
    val textProperty = ReadOnlyStringWrapper(status::class.simpleName)
    var text by textProperty

    val progressProperty = ReadOnlyDoubleWrapper(when (status) {
        TrainingScriptProgress.NotStarted -> 0.0
        TrainingScriptProgress.Creating -> ProgressBar.INDETERMINATE_PROGRESS
        TrainingScriptProgress.Initializing -> ProgressBar.INDETERMINATE_PROGRESS
        is TrainingScriptProgress.InProgress -> status.percentComplete
        TrainingScriptProgress.Completed -> 1.0
        is TrainingScriptProgress.Error -> 1.0
    })
    var progress by progressProperty

    val stateProperty = ReadOnlyObjectWrapper<State>(when (status) {
        TrainingScriptProgress.NotStarted -> State.NORMAL
        TrainingScriptProgress.Creating -> State.NORMAL
        TrainingScriptProgress.Initializing -> State.NORMAL
        is TrainingScriptProgress.InProgress -> State.NORMAL
        TrainingScriptProgress.Completed -> State.SUCCESS
        is TrainingScriptProgress.Error -> State.ERROR
    })
    var state by stateProperty
}

class TrainingScriptProgressModel: ItemViewModel<TrainingScriptProgressDto>() {
    val text = bind(TrainingScriptProgressDto::textProperty)
    val progress = bind(TrainingScriptProgressDto::progressProperty)
    val state = bind(TrainingScriptProgressDto::stateProperty)
}

enum class State {
    NORMAL, SUCCESS, ERROR
}