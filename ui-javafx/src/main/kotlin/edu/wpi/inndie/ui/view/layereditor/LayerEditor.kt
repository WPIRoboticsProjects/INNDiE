package edu.wpi.inndie.ui.view.layereditor

import arrow.core.getOrHandle
import com.fxgraph.edges.Edge
import com.fxgraph.graph.Graph
import com.fxgraph.layout.AbegoTreeLayout
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import javafx.scene.layout.BorderPane
import org.abego.treelayout.Configuration
import tornadofx.action
import tornadofx.add
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.checkbox
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.splitpane
import tornadofx.textfield

@Suppress("UnstableApiUsage")
class LayerEditor(
    private val initialModel: Model
) : BorderPane() {

    private val graph = Graph()

    init {
        graph.beginUpdate()

        when (initialModel) {
            is Model.Sequential -> {
                initialModel.layers.fold<Layer.MetaLayer, LayerCell?>(null) { prevCell, layer ->
                    val cell = createLayerCell(
                        layer,
                        ::openEditor
                    )
                    graph.model.addCell(cell)
                    prevCell?.let { graph.model.addEdge(it, cell) }
                    cell
                }
            }

            is Model.General -> {
                val cells = initialModel.layers
                    .nodes()
                    .map { it to createLayerCell(
                        it,
                        ::openEditor
                    )
                    }
                    .toMap()

                val edges = initialModel.layers.edges().map {
                    Edge(cells[it.nodeU()], cells[it.nodeV()])
                }

                cells.values.forEach { graph.model.addCell(it) }
                edges.forEach { graph.model.addEdge(it) }
            }
        }

        graph.endUpdate()
        graph.layout(AbegoTreeLayout(100.0, 100.0, Configuration.Location.Top))
        center = splitpane {
            // The canvas has to be in a splitpane, or else it could draw the graph on top of other
            // UI elements
            add(graph.canvas)
        }
    }

    fun getNewModel(): Model = when (initialModel) {
        is Model.Sequential -> {
            val newLayers = graph.model.allCells.mapTo(mutableSetOf()) {
                (it as LayerCell).layer.item
            }

            initialModel.copy(layers = newLayers)
        }

        is Model.General -> {
            val layers = graph.model.allCells.mapTo(mutableSetOf()) { (it as LayerCell).layer }
            val edges = graph.model.allEdges.map {
                (it.source as LayerCell).layer to (it.target as LayerCell).layer
            }

            val newLayers = layers.mapTo(mutableSetOf()) { layer ->
                val inputs = edges.filter { it.second == layer }.mapTo(mutableSetOf()) {
                    it.first.item.name
                }
                layer.item.copyWithNewInputs(inputs)
            }

            initialModel.copy(
                layers = DefaultLayersToGraph()
                    .convertToGraph(newLayers)
                    .getOrHandle { error(it) }
            )
        }
    }

    private fun openEditor(layerCell: LayerCell) {
        right = when (val layer = layerCell.layer.item) {
            is Layer.MetaLayer.TrainableLayer -> form {
                val model = layerCell.layer as TrainableLayerModel
                fieldset("Edit Layer") {
                    field("Type") {
                        textfield(layer.layer::class.simpleName) {
                            isEditable = false
                        }
                    }
                    field("Name") {
                        textfield(layer.name) {
                            isEditable = false
                        }
                    }
                    field("Trainable") {
                        checkbox(property = model.trainableProperty)
                    }
                    button("Save") {
                        enableWhen(model.dirty)
                        action {
                            model.commit()
                            layerCell.content =
                                createBaseLayerCell(
                                    model.item.layer
                                )
                        }
                    }
                    button("Close") {
                        action {
                            right = null
                        }
                    }
                }
            }

            is Layer.MetaLayer.UntrainableLayer -> form {
                val model =
                    UntrainableLayerModel(layer)
                fieldset("Edit Layer") {
                    field("Type") {
                        textfield(layer.layer::class.simpleName) {
                            isEditable = false
                        }
                    }
                    field("Name") {
                        textfield(layer.name) {
                            isEditable = false
                        }
                    }
                    buttonbar {
                        button("Save") {
                            enableWhen(model.dirty)
                            action {
                                model.commit()
                                layerCell.content =
                                    createBaseLayerCell(
                                        model.item.layer
                                    )
                            }
                        }
                        button("Close") {
                            action {
                                right = null
                            }
                        }
                    }
                }
            }
        }
    }
}
