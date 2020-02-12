package edu.wpi.axon.ui.view

import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.View
import tornadofx.action
import tornadofx.borderpane
import tornadofx.item
import tornadofx.menu
import tornadofx.menubar
import tornadofx.separator
import tornadofx.useMaxSize
import tornadofx.usePrefHeight
import tornadofx.vbox
import tornadofx.vgrow

class Main: View() {
    override val root = BorderPane()

    init {
        with(root) {
            top = menubar {
                isUseSystemMenuBar = true
                menu("File") {
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
            center = vbox {
                add<JobTable>()
            }
        }
    }
}
