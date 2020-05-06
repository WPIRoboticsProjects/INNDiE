package edu.wpi.inndie.ui.view.layereditor

import edu.wpi.inndie.tfdata.layer.Layer
import tornadofx.ItemViewModel

class UntrainableLayerModel(layer: Layer.MetaLayer.UntrainableLayer) :
    ItemViewModel<Layer.MetaLayer.UntrainableLayer>(layer)
