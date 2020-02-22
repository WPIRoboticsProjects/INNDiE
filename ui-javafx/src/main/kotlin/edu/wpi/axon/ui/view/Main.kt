package edu.wpi.axon.ui.view

import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.ui.model.JobModel
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import tornadofx.View
import tornadofx.action
import tornadofx.item
import tornadofx.label
import tornadofx.menu
import tornadofx.menubar
import tornadofx.separator
import tornadofx.vbox

class Main : View() {
    override val root = BorderPane()

    private val job by inject<JobModel>()

    init {
        with(root) {
            prefWidth = 1200.0
            prefHeight = 768.0

            top = menubar {
                isUseSystemMenuBar = true
                menu("File") {
                    item("Preferences", "Shortcut+,").action {
                        find<Preferences>().openWindow()
                    }
                    separator()
                    item("Exit").action {
                    }
                }
                menu("Help") {
                    item("About").action {
                        find<About>().openModal()
                    }
                }
            }
            left = vbox {
                add<JobList>()
            }
            center = vbox {
                add<JobManager>()
            }
        }
    }
}
