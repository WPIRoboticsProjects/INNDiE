package edu.wpi.axon.ui.view

import javafx.scene.layout.BorderPane
import tornadofx.View
import tornadofx.action
import tornadofx.borderpane
import tornadofx.item
import tornadofx.menu
import tornadofx.menubar
import tornadofx.separator
import tornadofx.vbox

class Main: View() {
    override val root = BorderPane()

    init {
        with(root) {
            top = menubar {
                menu("File") {
                    separator()
                    item("Exit").action {

                    }
                }
                menu("Help") {
                    item("About").action {
                        openInternalWindow<About>()
                    }
                }
            }
            center = vbox {
                add<JobTable>()
            }
        }
    }
}
