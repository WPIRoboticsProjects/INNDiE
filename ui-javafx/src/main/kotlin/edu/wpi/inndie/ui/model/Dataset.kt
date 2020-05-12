package edu.wpi.inndie.ui.model

import edu.wpi.inndie.tfdata.Dataset
import javafx.beans.property.SimpleObjectProperty
import tornadofx.ItemViewModel

class DatasetModel : ItemViewModel<Dataset>() {
    val type = bind { SimpleObjectProperty<DatasetType>(
            item.let {
                when (it) {
                    is Dataset.ExampleDataset -> DatasetType.EXAMPLE
                    is Dataset.Custom -> DatasetType.CUSTOM
                    else -> null
                }
            }
    ) }
}

enum class DatasetType {
    EXAMPLE, CUSTOM
}
