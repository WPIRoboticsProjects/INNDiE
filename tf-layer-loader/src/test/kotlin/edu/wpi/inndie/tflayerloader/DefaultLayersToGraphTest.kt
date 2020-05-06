@file:SuppressWarnings("LongMethod", "LargeClass", "StringLiteralDuplication")
@file:Suppress("UnstableApiUsage")

package edu.wpi.inndie.tflayerloader

import com.google.common.graph.EndpointPair
import edu.wpi.axon.tfdata.layer.Layer
import io.kotlintest.assertions.arrow.either.shouldBeLeft
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

internal class DefaultLayersToGraphTest {

    private val layersToGraph =
        DefaultLayersToGraph()

    @Test
    fun `no layers makes an empty graph`() {
        val graph = layersToGraph.convertToGraph(emptySet())
        graph.shouldBeRight {
            it.nodes() shouldHaveSize 0
        }
    }

    @Test
    fun `one layer makes a graph with one unconnected node`() {
        val layer1 = Layer.UnknownLayer("layer1", emptySet()).isTrainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1))
        graph.shouldBeRight {
            it.nodes().shouldContainExactly(layer1)
            it.edges() shouldHaveSize 0
        }
    }

    @Test
    fun `islands are not allowed`() {
        val layer1 = Layer.UnknownLayer("layer1", emptySet()).isTrainable()
        val layer2 = Layer.UnknownLayer("layer2", emptySet()).isTrainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer2))
        graph.shouldBeLeft()
    }

    @Test
    fun `two Sequential layers`() {
        val layer1 = Layer.UnknownLayer("layer1", null).isTrainable()
        val layer2 = Layer.UnknownLayer("layer2", null).isTrainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer2))
        graph.shouldBeLeft()
    }

    @Test
    fun `two layers with a connection`() {
        val layer1 = Layer.UnknownLayer("layer1", emptySet()).isTrainable()
        val layer2 = Layer.UnknownLayer("layer2", setOf("layer1")).isTrainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer2))
        graph.shouldBeRight {
            it.nodes().shouldContainExactly(layer1, layer2)
            it.edges().shouldContainExactly(EndpointPair.ordered(layer1, layer2))
        }
    }

    @Test
    fun `two connections to a layer`() {
        val layer1 = Layer.UnknownLayer("layer1", emptySet()).isTrainable()
        val layer2 = Layer.UnknownLayer("layer2", emptySet()).isTrainable()
        val layer3 = Layer.UnknownLayer("layer3", setOf("layer1", "layer2")).isTrainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer2, layer3))
        graph.shouldBeRight {
            it.nodes().shouldContainExactly(layer1, layer2, layer3)
            it.edges().shouldContainExactlyInAnyOrder(
                EndpointPair.ordered(layer1, layer3),
                EndpointPair.ordered(layer2, layer3)
            )
        }
    }

    @Test
    fun `duplicate layer names fails`() {
        val layer1 = Layer.UnknownLayer("layer1", emptySet()).isTrainable()
        val layer2 = Layer.UnknownLayer("layer1", setOf("layer2")).isTrainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer2))
        graph.shouldBeLeft()
    }

    @Test
    fun `layers missing inputs fails`() {
        val layer1 = Layer.UnknownLayer("layer1", emptySet()).isTrainable()
        val layer2 = Layer.UnknownLayer("layer2", null).isTrainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer2))
        graph.shouldBeLeft()
    }

    @Test
    fun `self loop fails`() {
        val layer1 = Layer.UnknownLayer("layer1", setOf("layer1")).isTrainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1))
        graph.shouldBeLeft()
    }

    @Test
    fun `inputs not in previous layers fails`() {
        val layer1 = Layer.InputLayer("l1", listOf())
        val layer2 = Layer.UnknownLayer("l2", setOf(layer1.name)).isTrainable()
        val layer3 = Layer.UnknownLayer("l3", setOf(layer2.name)).isTrainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer3))
        graph.shouldBeLeft()
    }
}
