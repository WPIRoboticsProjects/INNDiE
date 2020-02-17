package edu.wpi.axon.ui.model

import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.examplemodel.ExampleModel
import javafx.beans.property.SimpleObjectProperty
import tornadofx.ItemViewModel

class ModelSourceModel : ItemViewModel<ModelSource>() {
    val type = bind {
        SimpleObjectProperty<ModelSourceType>(
            item.let {
                when (it) {
                    is ModelSource.FromExample -> ModelSourceType.EXAMPLE
                    is ModelSource.FromFile -> ModelSourceType.FILE
                    is ModelSource.FromJob -> ModelSourceType.JOB
                    else -> null
                }
            }
        )
    }
}

enum class ModelSourceType {
    EXAMPLE, FILE, JOB
}
