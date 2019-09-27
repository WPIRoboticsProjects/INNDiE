package edu.wpi.axon.dsl.task

import arrow.core.Either
import arrow.core.Some
import arrow.core.extensions.either.monadError.monadError
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import edu.wpi.axon.dsl.alwaysValidImportValidator
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.dsl.mockVariableNameGenerator
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.code.layer.LayerToCode
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tfdata.layer.trainable
import edu.wpi.axon.tflayerloader.layerGraphIsValid
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.booleans.shouldBeTrue
import io.kotlintest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.octogonapus.ktguava.collections.toImmutableGraph

@Suppress("UnstableApiUsage")
internal class ApplyFunctionalLayerDeltaTaskTest : KoinTestFixture() {

    private fun layerGraph(
        vararg layers: Pair<SealedLayer.MetaLayer, SealedLayer.MetaLayer>
    ): ImmutableGraph<SealedLayer.MetaLayer> {
        layers.size.shouldBeGreaterThanOrEqual(1)

        val layerGraph =
            GraphBuilder.directed()
                .expectedNodeCount(layers.size)
                .allowsSelfLoops(false)
                .build<SealedLayer.MetaLayer>()

        layers.forEach { (fst, snd) ->
            layerGraph.putEdge(fst, snd)
        }

        // If this assertion fails, the layer graph is invalid so it should never reach the task
        Either.monadError<String>().layerGraphIsValid(layerGraph).fix().shouldBeRight()
        return layerGraph.toImmutableGraph()
    }

    private fun makeModel(
        vararg layers: Pair<SealedLayer.MetaLayer, SealedLayer.MetaLayer>,
        input: Set<SealedLayer.MetaLayer.UntrainableLayer>,
        output: SealedLayer.MetaLayer,
        name: String = ""
    ): Model.General {
        val layerGraph = layerGraph(*layers)

        return Model.General(
            name,
            input.mapTo(mutableSetOf()) { (it.layer as SealedLayer.InputLayer).toInputData() },
            layerGraph.toImmutableGraph(),
            setOf(Model.General.OutputData(output.name))
        )
    }

    @Test
    fun `current and new layers both with inputs are fine`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val input1 = SealedLayer.InputLayer("in1", listOf())
        val layer1 = SealedLayer.UnknownLayer("l1", Some(setOf())).trainable()

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentModel = makeModel(input1 to layer1, input = setOf(input1), output = layer1)
            newModel = makeModel(input1 to layer1, input = setOf(input1), output = layer1)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
    }

    @Test
    fun `keep all 2 layers`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
                mockVariableNameGenerator()
            })
        }

        val layer1 = SealedLayer.InputLayer("l1", listOf())
        val layer2 = SealedLayer.UnknownLayer("l2", Some(setOf(layer1.name))).trainable()

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentModel = makeModel(layer1 to layer2, input = setOf(layer1), output = layer2)
            newModel = makeModel(layer1 to layer2, input = setOf(layer1), output = layer2)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = base_model.inputs[0]
            |var2 = base_model.get_layer("l2")(var1)
            |new_model = tf.keras.Model(inputs=[var1], outputs=var2)
            |new_model.get_layer("l2").trainable = True
        """.trimMargin()
    }

    @Test
    fun `remove the last layer`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
                mockVariableNameGenerator()
            })
        }

        val layer1 = SealedLayer.InputLayer("l1", listOf())
        val layer2 = SealedLayer.UnknownLayer("l2", Some(setOf(layer1.name))).trainable()
        val layer3 = SealedLayer.UnknownLayer("l3", Some(setOf(layer2.name))).trainable()

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentModel = makeModel(
                layer1 to layer2,
                layer2 to layer3,
                input = setOf(layer1),
                output = layer3
            )
            newModel = makeModel(
                layer1 to layer2,
                input = setOf(layer1),
                output = layer2
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = base_model.inputs[0]
            |var2 = base_model.get_layer("l2")(var1)
            |new_model = tf.keras.Model(inputs=[var1], outputs=var2)
            |new_model.get_layer("l2").trainable = True
        """.trimMargin()
    }

    @Test
    fun `replace one layer`() {
        val layer1 = SealedLayer.InputLayer("l1", listOf())
        val layer2 = SealedLayer.UnknownLayer("l2", Some(setOf(layer1.name))).trainable()
        val layer3 =
            SealedLayer.Dense("l3", Some(setOf(layer1.name)), 10, Activation.SoftMax).trainable()

        val mockLayerToCode = mockk<LayerToCode> {
            every { makeNewLayer(layer3.layer) } returns "layer3".right()
        }

        startKoin {
            modules(module {
                alwaysValidImportValidator()
                mockVariableNameGenerator()
                single { mockLayerToCode }
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentModel = makeModel(
                layer1 to layer2,
                input = setOf(layer1),
                output = layer2
            )
            newModel = makeModel(
                layer1 to layer3,
                input = setOf(layer1),
                output = layer3
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = base_model.inputs[0]
            |var2 = layer3(var1)
            |new_model = tf.keras.Model(inputs=[var1], outputs=var2)
            |new_model.get_layer("l3").trainable = True
        """.trimMargin()

        verifyAll { mockLayerToCode.makeNewLayer(layer3.layer) }
        confirmVerified(mockLayerToCode)
    }

    @Test
    fun `replace a layer with an invalid layer`() {
        val layer1 = SealedLayer.InputLayer("l1", listOf())
        val layer2 = SealedLayer.UnknownLayer("l2", Some(setOf(layer1.name))).trainable()
        val layer3 = SealedLayer.UnknownLayer("l3", Some(setOf(layer1.name))).trainable()

        val mockLayerToCode = mockk<LayerToCode> {
            every { makeNewLayer(layer3.layer) } returns "".left()
        }

        startKoin {
            modules(module {
                alwaysValidImportValidator()
                mockVariableNameGenerator()
                single { mockLayerToCode }
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentModel = makeModel(layer1 to layer2, input = setOf(layer1), output = layer2)
            newModel = makeModel(layer1 to layer3, input = setOf(layer1), output = layer3)
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        shouldThrow<IllegalStateException> { task.code() }

        verifyAll { mockLayerToCode.makeNewLayer(layer3.layer) }
        confirmVerified(mockLayerToCode)
    }

    @Test
    fun `copy a layer with two inputs`() {
        val layer1 = SealedLayer.InputLayer("l1", listOf())
        val layer2 = SealedLayer.UnknownLayer("l2", Some(setOf(layer1.name))).trainable()
        val layer3 = SealedLayer.Dense(
            "l3",
            Some(setOf(layer1.name, layer2.name)),
            10,
            Activation.SoftMax
        ).trainable()

        startKoin {
            modules(module {
                alwaysValidImportValidator()
                mockVariableNameGenerator()
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentModel = makeModel(
                layer1 to layer2,
                layer1 to layer3,
                layer2 to layer3,
                input = setOf(layer1),
                output = layer3
            )
            newModel = makeModel(
                layer1 to layer2,
                layer1 to layer3,
                layer2 to layer3,
                input = setOf(layer1),
                output = layer3
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = base_model.inputs[0]
            |var2 = base_model.get_layer("l2")(var1)
            |var3 = base_model.get_layer("l3")([var1, var2])
            |new_model = tf.keras.Model(inputs=[var1], outputs=var3)
            |new_model.get_layer("l2").trainable = True
            |new_model.get_layer("l3").trainable = True
        """.trimMargin()
    }

    @Test
    fun `two model inputs are separated`() {
        val input1 = SealedLayer.InputLayer("in1", listOf())
        val input2 = SealedLayer.InputLayer("in2", listOf())
        val layer1 =
            SealedLayer.UnknownLayer("l1", Some(setOf(input1.name, input2.name))).trainable()

        startKoin {
            modules(module {
                alwaysValidImportValidator()
                mockVariableNameGenerator()
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentModel = makeModel(
                input1 to layer1,
                input2 to layer1,
                input = setOf(input1, input2),
                output = layer1
            )
            newModel = makeModel(
                input1 to layer1,
                input2 to layer1,
                input = setOf(input1, input2),
                output = layer1
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = base_model.inputs[0]
            |var2 = base_model.inputs[1]
            |var3 = base_model.get_layer("l1")([var1, var2])
            |new_model = tf.keras.Model(inputs=[var1, var2], outputs=var3)
            |new_model.get_layer("l1").trainable = True
        """.trimMargin()
    }

    @Test
    fun `swap two model inputs`() {
        val input1 = SealedLayer.InputLayer("in1", listOf())
        val input2 = SealedLayer.InputLayer("in2", listOf())
        val layer1 =
            SealedLayer.UnknownLayer("l1", Some(setOf(input1.name, input2.name))).trainable()

        startKoin {
            modules(module {
                alwaysValidImportValidator()
                mockVariableNameGenerator()
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentModel = makeModel(
                input1 to layer1,
                input2 to layer1,
                input = setOf(input1, input2),
                output = layer1
            )
            newModel = makeModel(
                input2 to layer1,
                input1 to layer1,
                input = setOf(input2, input1),
                output = layer1
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = base_model.inputs[1]
            |var2 = base_model.inputs[0]
            |var3 = base_model.get_layer("l1")([var2, var1])
            |new_model = tf.keras.Model(inputs=[var1, var2], outputs=var3)
            |new_model.get_layer("l1").trainable = True
        """.trimMargin()
    }

    @Test
    fun `reorder three model inputs`() {
        val input1 = SealedLayer.InputLayer("in1", listOf())
        val input2 = SealedLayer.InputLayer("in2", listOf())
        val input3 = SealedLayer.InputLayer("in3", listOf())
        val layer1 =
            SealedLayer.UnknownLayer("l1", Some(setOf(input1.name, input2.name))).trainable()

        startKoin {
            modules(module {
                alwaysValidImportValidator()
                mockVariableNameGenerator()
            })
        }

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            currentModel = makeModel(
                input1 to layer1,
                input2 to layer1,
                input3 to layer1,
                input = setOf(input1, input2, input3),
                output = layer1
            )
            newModel = makeModel(
                input2 to layer1,
                input3 to layer1,
                input1 to layer1,
                input = setOf(input2, input3, input1),
                output = layer1
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = base_model.inputs[1]
            |var2 = base_model.inputs[2]
            |var3 = base_model.inputs[0]
            |var4 = base_model.get_layer("l1")([var3, var1])
            |new_model = tf.keras.Model(inputs=[var1, var2, var3], outputs=var4)
            |new_model.get_layer("l1").trainable = True
        """.trimMargin()
    }
}
