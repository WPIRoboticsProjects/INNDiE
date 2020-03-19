package edu.wpi.axon.ui.view.layereditor

import com.fxgraph.cells.AbstractCell
import com.fxgraph.graph.Graph
import edu.wpi.axon.tfdata.layer.Layer
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import tornadofx.ItemViewModel
import tornadofx.hbox
import tornadofx.label

fun createLayerCell(layer: Layer.MetaLayer, openEditor: (LayerCell) -> Unit) = when (layer) {
    is Layer.MetaLayer.TrainableLayer -> LayerCell(
        TrainableLayerModel(layer),
        createBaseLayerCell(layer.layer),
        openEditor
    )

    is Layer.MetaLayer.UntrainableLayer -> LayerCell(
        UntrainableLayerModel(layer),
        createBaseLayerCell(layer.layer),
        openEditor
    )
}

fun createBaseLayerCell(layer: Layer): VBox = when (layer) {
    is Layer.MetaLayer -> error("This method only accepts Layer (not MetaLayer).")
    is Layer.UnknownLayer -> layerVBox { }
    is Layer.ModelLayer -> layerVBox { }
    is Layer.InputLayer -> layerVBox {
        layerProp(
            "Input Shape:",
            layer.batchInputShape.joinToString(separator = "x") { it?.toString() ?: "?" }
        )
    }
    is Layer.BatchNormalization -> layerVBox { }
    is Layer.AveragePooling2D -> layerVBox { }
    is Layer.Conv2D -> layerVBox { }
    is Layer.Dense -> layerVBox {
        layerProp("Units:", layer.units.toString())
    }
    is Layer.Dropout -> layerVBox { }
    is Layer.Flatten -> layerVBox { }
    is Layer.GlobalAveragePooling2D -> layerVBox { }
    is Layer.GlobalMaxPooling2D -> layerVBox { }
    is Layer.MaxPooling2D -> layerVBox { }
    is Layer.SpatialDropout2D -> layerVBox { }
    is Layer.UpSampling2D -> layerVBox { }
}

private fun layerVBox(configure: VBox.() -> Unit) = VBox().apply {
    spacing = 5.0
    alignment = Pos.TOP_LEFT
    configure()
}

private fun Node.layerProp(name: String, value: String) = hbox {
    spacing = 5.0
    label(name) { style = "-fx-text-weight: bold;" }
    label(value)
}

fun createLayerColor(layer: Layer): String = when (layer) {
    is Layer.MetaLayer.TrainableLayer -> createLayerColor(
        layer.layer
    )
    is Layer.MetaLayer.UntrainableLayer -> createLayerColor(
        layer.layer
    )
    is Layer.UnknownLayer -> "#000000"
    is Layer.ModelLayer -> "#db0808"
    is Layer.InputLayer -> "#000000"
    is Layer.BatchNormalization -> "#ede221"
    is Layer.AveragePooling2D -> "#f48f4b"
    is Layer.Conv2D -> "#25cee8"
    is Layer.Dense -> "#1889e0"
    is Layer.Dropout -> "#6e27b5"
    is Layer.Flatten -> "#b24f0e"
    is Layer.GlobalAveragePooling2D -> "#5df4d1"
    is Layer.GlobalMaxPooling2D -> "#03a340"
    is Layer.MaxPooling2D -> "#0d9919"
    is Layer.SpatialDropout2D -> "#e5fc3a"
    is Layer.UpSampling2D -> "#e7e8e3"
}

class LayerCell(
    var layer: ItemViewModel<out Layer.MetaLayer>,
    var content: Node,
    private val openEditor: (LayerCell) -> Unit
) : AbstractCell() {

    override fun getGraphic(graph: Graph) = BorderPane().apply {
        style = "-fx-border-color: black; -fx-background-color: white;"

        setOnMouseClicked {
            openEditor(this@LayerCell)
        }

        top = hbox {
            style = "-fx-background-color: ${createLayerColor(
                layer.item
            )};"

            label(layer.item.layer::class.simpleName!!) {
                style = "-fx-text-fill: white;"
            }
        }

        center = content
    }
}
