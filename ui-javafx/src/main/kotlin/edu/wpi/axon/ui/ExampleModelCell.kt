package edu.wpi.axon.ui

import edu.wpi.axon.examplemodel.ExampleModel
import javafx.scene.control.ListCell

class ExampleModelCell : ListCell<ExampleModel>() {

    override fun updateItem(item: ExampleModel?, empty: Boolean) {
        super.updateItem(item, empty)
        text = if (empty || item == null) {
            null
        } else {
            item.name
        }
    }
}
