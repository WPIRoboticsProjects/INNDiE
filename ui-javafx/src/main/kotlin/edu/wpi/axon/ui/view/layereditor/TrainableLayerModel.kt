package edu.wpi.axon.ui.view.layereditor

import edu.wpi.axon.tfdata.layer.Layer
import javafx.beans.property.SimpleBooleanProperty
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue

class TrainableLayerModel(layer: Layer.MetaLayer.TrainableLayer) :
    ItemViewModel<Layer.MetaLayer.TrainableLayer>() {

    init {
        itemProperty.set(layer)
    }

    val trainableProperty = bind { SimpleBooleanProperty(item.trainable) }
    var trainable by trainableProperty

    override fun onCommit() {
        itemProperty.set(item.copy(trainable = trainable))
    }
}
