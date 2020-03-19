package edu.wpi.axon.ui.view.layereditor

import edu.wpi.axon.tfdata.layer.Layer
import tornadofx.ItemViewModel

class UntrainableLayerModel(layer: Layer.MetaLayer.UntrainableLayer) :
    ItemViewModel<Layer.MetaLayer.UntrainableLayer>(layer)
