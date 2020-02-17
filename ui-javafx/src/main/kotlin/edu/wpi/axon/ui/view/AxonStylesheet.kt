package edu.wpi.axon.ui.view

import tornadofx.Stylesheet
import tornadofx.px

class AxonStylesheet : Stylesheet() {
    init {
        root {
            prefHeight = 600.px
            prefWidth = 800.px
        }
    }
}
