package edu.wpi.inndie.ui.view

import edu.wpi.axon.ui.view.joblist.JobList
import edu.wpi.axon.ui.view.preferences.PreferencesView
import javafx.scene.layout.BorderPane
import tornadofx.View
import tornadofx.action
import tornadofx.center
import tornadofx.item
import tornadofx.left
import tornadofx.menu
import tornadofx.menubar
import tornadofx.separator

class Main : View() {

    override val root = BorderPane()

    init {
        with(root) {
            prefWidth = 1200.0
            prefHeight = 768.0

            top = menubar {
                isUseSystemMenuBar = true
                menu("File") {
                    item("Preferences", "Shortcut+,").action {
                        find<PreferencesView>().openWindow()
                    }
                    separator()
                    item("Exit", "Shortcut+W").action {
                        close()
                    }
                }
                menu("Help") {
                    item("About").action {
                        find<About>().openModal()
                    }
                }
            }
            left {
                add<JobList>()
            }
            center {
                add<JobManager>()
            }
        }
    }
}
