package edu.wpi.axon.ui

import edu.wpi.axon.ui.view.TrainingView
import javafx.application.Application
import tornadofx.App

class Axon: App(TrainingView::class) {
}

fun main(args: Array<String>) {
    Application.launch(Axon::class.java, *args)
}
