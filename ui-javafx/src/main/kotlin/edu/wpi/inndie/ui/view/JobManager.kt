package edu.wpi.inndie.ui.view

import edu.wpi.inndie.ui.view.jobeditor.JobEditor
import edu.wpi.inndie.ui.view.jobresult.JobResultsView
import edu.wpi.inndie.ui.view.jobtestview.JobTestView
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
