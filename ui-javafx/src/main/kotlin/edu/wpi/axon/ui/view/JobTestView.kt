package edu.wpi.axon.ui.view

import edu.wpi.axon.ui.model.JobModel
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import tornadofx.Fragment
import tornadofx.borderpane
import tornadofx.objectBinding

class JobTestView : Fragment() {

    private val job by inject<JobModel>()

    override val root = borderpane {
        centerProperty().bind(job.itemProperty.objectBinding {
            if (it == null) {
                Label("No selection.")
            } else {
                Label("Test view.")
            }
        })
    }
}
