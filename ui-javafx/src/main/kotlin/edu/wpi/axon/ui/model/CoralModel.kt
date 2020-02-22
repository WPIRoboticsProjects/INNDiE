package edu.wpi.axon.ui.model

import edu.wpi.axon.training.ModelDeploymentTarget
import javafx.beans.property.Property
import javafx.beans.property.SimpleDoubleProperty
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue

class CoralDto(target: ModelDeploymentTarget.Coral) {
    private val representativeDatasetPercentageProperty =
        SimpleDoubleProperty(target.representativeDatasetPercentage)
    var representativeDatasetPercentage by representativeDatasetPercentageProperty
}

class CoralModel(private val coralToSet: Property<ModelDeploymentTarget.Coral>) :
    ItemViewModel<CoralDto>() {
    val representativeDatasetPercentage = bind(CoralDto::representativeDatasetPercentage)

    override fun onCommit() {
        coralToSet.value = coralToSet.value.copy(
            representativeDatasetPercentage = representativeDatasetPercentage.value
        )
    }
}
