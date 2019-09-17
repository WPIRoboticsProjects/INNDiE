package edu.wpi.axon.ui.view

import javafx.stage.FileChooser
import tornadofx.*

class TrainingView: View("Train Model") {
    override val root = form {
        fieldset {
            field("userModelPath") {
                textfield()
                button {
                    action {
                        chooseFile(title = "Open Model", filters = arrayOf(FileChooser.ExtensionFilter("TensorFlow Model", ".hd5")))
                    }
                }
            }
            field("userDataset") {
                textfield()
            }
            field("userOptimizer") {
                textfield()
            }
            field("userLoss") {
                textfield()
            }
            field("userEpochs") {
                spinner(min = 1, initialValue = 5)
            }
            field("generateDebugComments") {
                checkbox()
            }
        }
        button("Save") {

        }
    }
}