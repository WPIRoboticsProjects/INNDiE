@file:SuppressWarnings("LongMethod", "LargeClass", "StringLiteralDuplication")
@file:Suppress("UnstableApiUsage")

package edu.wpi.axon.tflayerloader

import arrow.core.None
import arrow.core.Some
import com.google.common.graph.EndpointPair
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tfdata.layer.trainable
import io.kotlintest.assertions.arrow.either.shouldBeLeft
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

internal class DefaultLayersToGraphTest {

    private val layersToGraph = DefaultLayersToGraph()

    @Test
    fun `no layers makes an empty graph`() {
        val graph = layersToGraph.convertToGraph(emptySet())
        graph.shouldBeRight {
            it.nodes() shouldHaveSize 0
        }
    }

    @Test
    fun `one layer makes a graph with one unconnected node`() {
        val layer1 = SealedLayer.UnknownLayer("layer1", Some(emptySet())).trainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1))
        graph.shouldBeRight {
            it.nodes().shouldContainExactly(layer1)
            it.edges() shouldHaveSize 0
        }
    }

    @Test
    fun `islands are not allowed`() {
        val layer1 = SealedLayer.UnknownLayer("layer1", Some(emptySet())).trainable()
        val layer2 = SealedLayer.UnknownLayer("layer2", Some(emptySet())).trainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer2))
        graph.shouldBeLeft()
    }

    @Test
    fun `two Sequential layers`() {
        val layer1 = SealedLayer.UnknownLayer("layer1", None).trainable()
        val layer2 = SealedLayer.UnknownLayer("layer2", None).trainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer2))
        graph.shouldBeLeft()
    }

    @Test
    fun `two layers with a connection`() {
        val layer1 = SealedLayer.UnknownLayer("layer1", Some(emptySet())).trainable()
        val layer2 = SealedLayer.UnknownLayer("layer2", Some(setOf("layer1"))).trainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer2))
        graph.shouldBeRight {
            it.nodes().shouldContainExactly(layer1, layer2)
            it.edges().shouldContainExactly(EndpointPair.ordered(layer1, layer2))
        }
    }

    @Test
    fun `two connections to a layer`() {
        val layer1 = SealedLayer.UnknownLayer("layer1", Some(emptySet())).trainable()
        val layer2 = SealedLayer.UnknownLayer("layer2", Some(emptySet())).trainable()
        val layer3 = SealedLayer.UnknownLayer("layer3", Some(setOf("layer1", "layer2"))).trainable()
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
        val layer1 = SealedLayer.UnknownLayer("layer1", Some(emptySet())).trainable()
        val layer2 = SealedLayer.UnknownLayer("layer1", Some(setOf("layer2"))).trainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer2))
        graph.shouldBeLeft()
    }

    @Test
    fun `layers missing inputs fails`() {
        val layer1 = SealedLayer.UnknownLayer("layer1", Some(emptySet())).trainable()
        val layer2 = SealedLayer.UnknownLayer("layer2", None).trainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer2))
        graph.shouldBeLeft()
    }

    @Test
    fun `self loop fails`() {
        val layer1 = SealedLayer.UnknownLayer("layer1", Some(setOf("layer1"))).trainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1))
        graph.shouldBeLeft()
    }

    @Test
    fun `inputs not in previous layers fails`() {
        val layer1 = SealedLayer.InputLayer("l1", listOf())
        val layer2 = SealedLayer.UnknownLayer("l2", Some(setOf(layer1.name))).trainable()
        val layer3 = SealedLayer.UnknownLayer("l3", Some(setOf(layer2.name))).trainable()
        val graph = layersToGraph.convertToGraph(setOf(layer1, layer3))
        graph.shouldBeLeft()
    }
}
