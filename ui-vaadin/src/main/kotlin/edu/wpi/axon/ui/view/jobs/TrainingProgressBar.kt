package edu.wpi.axon.ui.view.jobs

import com.vaadin.flow.component.progressbar.ProgressBar
import com.vaadin.flow.component.progressbar.ProgressBarVariant
import edu.wpi.axon.dbdata.TrainingScriptProgress

class TrainingProgressBar(value: TrainingScriptProgress? = null) : ProgressBar() {
    init {
        value?.let { setValue(it) }
    }

    fun setValue(value: TrainingScriptProgress) {
        setValue(
            when (value) {
                TrainingScriptProgress.NotStarted -> {
                    isIndeterminate = false
                    themeName = ""
                    min
                }

                TrainingScriptProgress.Creating -> {
                    isIndeterminate = true
                    themeName = ProgressBarVariant.LUMO_CONTRAST.variantName
                    min
                }

                TrainingScriptProgress.Initializing -> {
                    isIndeterminate = true
                    themeName = ""
                    max
                }

                is TrainingScriptProgress.InProgress -> {
                    isIndeterminate = false
                    themeName = ""
                    value.percentComplete
                }

                TrainingScriptProgress.Completed -> {
                    isIndeterminate = false
                    themeName = ProgressBarVariant.LUMO_SUCCESS.variantName
                    max
                }

                TrainingScriptProgress.Error -> {
                    isIndeterminate = false
                    themeName = ProgressBarVariant.LUMO_ERROR.variantName
                    max
                }
            }
        )
    }
}
