package edu.wpi.axon.ui.view

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
    }
}
