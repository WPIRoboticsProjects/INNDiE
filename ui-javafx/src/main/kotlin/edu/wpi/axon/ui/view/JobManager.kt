package edu.wpi.axon.ui.view

import edu.wpi.axon.ui.view.jobeditor.JobEditor
import edu.wpi.axon.ui.view.jobresult.JobResultsView
import edu.wpi.axon.ui.view.jobtestview.JobTestView
import javafx.scene.control.TabPane
import tornadofx.Fragment
import tornadofx.tab
import tornadofx.tabpane

class JobManager : Fragment() {
    override val root = tabpane {
        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

        tab("Editor") {
            add<JobEditor>()
        }

        tab("Results") {
            add<JobResultsView>()
        }

        tab("Test") {
            add<JobTestView>()
        }
    }
}
