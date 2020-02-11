package edu.wpi.axon.ui.view

import com.fxgraph.cells.AbstractCell
import com.fxgraph.edges.Edge
import com.fxgraph.graph.Graph
import com.fxgraph.layout.AbegoTreeLayout
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.ui.MainUI
import org.abego.treelayout.Configuration
import tornadofx.Fragment
import tornadofx.borderpane
import tornadofx.splitpane

@Suppress("UnstableApiUsage")
class LayerEditor : Fragment("My View") {

    override val root = borderpane {
        val modelName = "network_with_add.h5"
        val (model, _) = MainUI.loadModel(modelName)

        val graph = Graph()
        graph.beginUpdate()

        when (model) {
            is Model.Sequential -> {
                model.layers.fold<Layer.MetaLayer, AbstractCell?>(null) { prevCell, layer ->
                    val cell = createLayerCell(layer)
                    graph.model.addCell(cell)
                    prevCell?.let { graph.model.addEdge(it, cell) }
                    cell
                }
            }

            is Model.General -> {
                val cells = model.layers.nodes().map { it to createLayerCell(it) }.toMap()
                val edges = model.layers.edges().map { Edge(cells[it.nodeU()], cells[it.nodeV()]) }
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
}
