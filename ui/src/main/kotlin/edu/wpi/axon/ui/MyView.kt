package edu.wpi.axon.ui

import tornadofx.View
import tornadofx.button
import tornadofx.label
import tornadofx.vbox

class MyView: View() {
    override val root = vbox {
        button("Press me")
        label("Waiting")
    }
}