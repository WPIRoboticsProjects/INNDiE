package edu.wpi.axon.ui.view

import tornadofx.View
import tornadofx.text
import tornadofx.vbox

class About: View() {
    override val root = vbox {
        setMinSize(400.0, 400.0)
        text("Axon")
    }
}
