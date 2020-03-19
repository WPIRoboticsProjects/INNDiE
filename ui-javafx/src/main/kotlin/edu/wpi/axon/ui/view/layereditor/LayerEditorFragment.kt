package edu.wpi.axon.ui.view.layereditor

import edu.wpi.axon.ui.model.JobModel
import tornadofx.Fragment
import tornadofx.action
import tornadofx.borderpane
import tornadofx.button
import tornadofx.buttonbar

class LayerEditorFragment : Fragment() {

    private val job by inject<JobModel>()

    override val root = borderpane {
        val layerEditor = LayerEditor(job.userNewModel.value)
        center = layerEditor

        bottom = buttonbar {
            button("Save") {
                action {
                    val newModel = layerEditor.getNewModel()
                    job.userNewModel.value = null
                    job.userNewModel.value = newModel
                    close()
                }
            }
            button("Cancel") {
                action {
                    close()
                }
            }
        }
    }
}
