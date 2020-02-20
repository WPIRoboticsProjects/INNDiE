package edu.wpi.axon.ui.view

import tornadofx.Stylesheet
import tornadofx.px

class AxonStylesheet : Stylesheet() {
    init {
        root {
            prefHeight = 768.px
            prefWidth = 1024.px
        }
    }
}
