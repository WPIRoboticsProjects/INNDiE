package edu.wpi.axon.ui.view

import edu.wpi.axon.ui.MainUI
import tornadofx.View
import tornadofx.action
import tornadofx.borderpane
import tornadofx.item
import tornadofx.menu
import tornadofx.menubar
import tornadofx.separator
import tornadofx.vbox

class Main : View() {

    override val root = borderpane {
        top = menubar {
            isUseSystemMenuBar = true
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
            setPrefSize(500.0, 400.0)
            // add<JobTable>()
            val modelName = "network_with_add.h5"
            val (model, _) = MainUI.loadModel(modelName)
            add(LayerEditor(model))
        }
    }
}
