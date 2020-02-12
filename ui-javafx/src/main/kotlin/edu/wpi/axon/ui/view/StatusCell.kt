package edu.wpi.axon.ui.view

import edu.wpi.axon.ui.model.JobDto
import edu.wpi.axon.ui.model.State
import edu.wpi.axon.ui.model.TrainingScriptProgressDto
import edu.wpi.axon.ui.model.TrainingScriptProgressModel
import tornadofx.TableCellFragment
import tornadofx.bindTo
import tornadofx.label
import tornadofx.onChange
import tornadofx.progressbar
import tornadofx.vbox

class StatusCell: TableCellFragment<JobDto, TrainingScriptProgressDto>() {
    private val status = TrainingScriptProgressModel().bindTo(this)

    override val root = vbox {
        label(status.text)

        progressbar(status.progress) {
            maxWidth = Double.MAX_VALUE
            status.state.onChange {
                it?.let {
                    styleClass.setAll(when (it) {
                        State.NORMAL -> listOf("progress-bar")
                        State.SUCCESS -> listOf("progress-bar-success", "progress-bar")
                        State.ERROR -> listOf("progress-bar-error", "progress-bar")
                    })
                }
            }
        }
    }
}
