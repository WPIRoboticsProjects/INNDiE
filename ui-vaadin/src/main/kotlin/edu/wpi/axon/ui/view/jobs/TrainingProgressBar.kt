package edu.wpi.axon.ui.view.jobs

import com.vaadin.flow.component.progressbar.ProgressBar
import com.vaadin.flow.component.progressbar.ProgressBarVariant
import edu.wpi.axon.dbdata.TrainingScriptProgress

class TrainingProgressBar(value: TrainingScriptProgress? = null) : ProgressBar() {
    init {
        value?.let { setValue(it) }
    }

    fun setValue(value: TrainingScriptProgress) {
        when (value) {
            is TrainingScriptProgress.NotStarted -> {
                setValue(max)
                addThemeVariants(ProgressBarVariant.LUMO_CONTRAST)
            }
            is TrainingScriptProgress.InProgress -> {
                setValue(value.percentComplete)
                removeThemeVariants(ProgressBarVariant.LUMO_SUCCESS)
            }
            TrainingScriptProgress.Completed -> {
                setValue(max)
                addThemeVariants(ProgressBarVariant.LUMO_SUCCESS)
            }
        }
    }
}
