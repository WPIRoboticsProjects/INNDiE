package edu.wpi.axon.ui.view

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import javafx.collections.FXCollections
import javafx.stage.FileChooser
import tornadofx.*
import kotlin.reflect.KClass

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
                combobox<KClass<out Dataset>> {
                    items = FXCollections.observableArrayList(Dataset::class.sealedSubclasses)
                    cellFormat {
                        text = item.simpleName
                    }
                }
            }
            field("userOptimizer") {
                combobox<KClass<out Optimizer>> {
                    items = FXCollections.observableArrayList(Optimizer::class.sealedSubclasses)
                    cellFormat {
                        text = item.simpleName
                    }
                }
            }
            field("userLoss") {
                combobox<KClass<out Loss>> {
                    items = FXCollections.observableArrayList(Loss::class.sealedSubclasses)
                    cellFormat {
                        text = item.simpleName
                    }
                }
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
