@file:SuppressWarnings("LongMethod", "LargeClass", "TooManyFunctions")

package edu.wpi.axon.dsl.task

import arrow.core.Either
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
import edu.wpi.axon.tfdata.layer.Layer
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

@Suppress("UnstableApiUsage", "StringLiteralDuplication")
internal class ApplyFunctionalLayerDeltaTaskTest : KoinTestFixture() {

    private fun layerGraph(
        vararg layers: Pair<Layer.MetaLayer, Layer.MetaLayer>
    ): ImmutableGraph<Layer.MetaLayer> {
        layers.size.shouldBeGreaterThanOrEqual(1)

        val layerGraph =
            GraphBuilder.directed()
                .expectedNodeCount(layers.size)
                .allowsSelfLoops(false)
                .build<Layer.MetaLayer>()

        layers.forEach { (fst, snd) ->
            layerGraph.putEdge(fst, snd)
        }

        // If this assertion fails, the layer graph is invalid so it should never reach the task
        Either.monadError<String>().layerGraphIsValid(layerGraph).fix().shouldBeRight()
        return layerGraph.toImmutableGraph()
    }

    @Suppress("SpreadOperator")
    private fun makeModel(
        vararg layers: Pair<Layer.MetaLayer, Layer.MetaLayer>,
        input: Set<Layer.MetaLayer.UntrainableLayer>,
        output: Set<Layer.MetaLayer>,
        name: String = ""
    ): Model.General {
        val layerGraph = layerGraph(*layers)

        return Model.General(
            name,
            input.mapTo(mutableSetOf()) { (it.layer as Layer.InputLayer).toInputData() },
            layerGraph.toImmutableGraph(),
            output.mapTo(mutableSetOf()) { Model.General.OutputData(it.name) }
        )
    }

    @Test
    fun `change layer trainable flag`() {
        val input1 = Layer.InputLayer("l1", listOf(null, 1))
        val layer2 = Layer.UnknownLayer("l2", setOf(input1.name)).isTrainable()
        val newLayer2 = Layer.UnknownLayer("l2", setOf(input1.name)).isTrainable(false)

        val mockLayerToCode = mockk<LayerToCode> {
            every { makeNewLayer(Layer.InputLayer("l1", listOf(1)).layer) } returns "input1".right()
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
            oldModel = makeModel(
                input1 to layer2,
                input = setOf(input1),
                output = setOf(layer2)
            )
            newModel = makeModel(
                input1 to newLayer2,
                input = setOf(input1),
                output = setOf(newLayer2)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = input1
            |var2 = base_model.get_layer("l2")(var1)
            |new_model = tf.keras.Model(inputs=[var1], outputs=[var2])
            |new_model.get_layer("l2").trainable = False
        """.trimMargin()

        verifyAll { mockLayerToCode.makeNewLayer(Layer.InputLayer("l1", listOf(1)).layer) }
        confirmVerified(mockLayerToCode)
    }

    @Test
    fun `current and new layers both with inputs are fine`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val input1 = Layer.InputLayer("in1", listOf(null, 1))
        val layer1 = Layer.UnknownLayer("l1", setOf()).isTrainable()

        val task = ApplyFunctionalLayerDeltaTask("").apply {
            modelInput = configuredCorrectly("base_model")
            oldModel =
                makeModel(input1 to layer1, input = setOf(input1), output = setOf(layer1))
            newModel = makeModel(input1 to layer1, input = setOf(input1), output = setOf(layer1))
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
    }

    @Test
    fun `keep all 2 layers`() {
        val input1 = Layer.InputLayer("l1", listOf(null, 1))
        val layer2 = Layer.UnknownLayer("l2", setOf(input1.name)).isTrainable()

        val mockLayerToCode = mockk<LayerToCode> {
            every { makeNewLayer(Layer.InputLayer("l1", listOf(1)).layer) } returns "input1".right()
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
            oldModel =
                makeModel(input1 to layer2, input = setOf(input1), output = setOf(layer2))
            newModel = makeModel(input1 to layer2, input = setOf(input1), output = setOf(layer2))
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = input1
            |var2 = base_model.get_layer("l2")(var1)
            |new_model = tf.keras.Model(inputs=[var1], outputs=[var2])
            |new_model.get_layer("l2").trainable = True
        """.trimMargin()

        verifyAll { mockLayerToCode.makeNewLayer(Layer.InputLayer("l1", listOf(1)).layer) }
        confirmVerified(mockLayerToCode)
    }

    @Test
    fun `remove the last layer`() {
        val input1 = Layer.InputLayer("l1", listOf(null, 1))
        val layer2 = Layer.UnknownLayer("l2", setOf(input1.name)).isTrainable()
        val layer3 = Layer.UnknownLayer("l3", setOf(layer2.name)).isTrainable()

        val mockLayerToCode = mockk<LayerToCode> {
            every { makeNewLayer(Layer.InputLayer("l1", listOf(1)).layer) } returns "input1".right()
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
            oldModel = makeModel(
                input1 to layer2,
                layer2 to layer3,
                input = setOf(input1),
                output = setOf(layer3)
            )
            newModel = makeModel(
                input1 to layer2,
                input = setOf(input1),
                output = setOf(layer2)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = input1
            |var2 = base_model.get_layer("l2")(var1)
            |new_model = tf.keras.Model(inputs=[var1], outputs=[var2])
            |new_model.get_layer("l2").trainable = True
        """.trimMargin()

        verifyAll { mockLayerToCode.makeNewLayer(Layer.InputLayer("l1", listOf(1)).layer) }
        confirmVerified(mockLayerToCode)
    }

    @Test
    fun `replace one layer`() {
        val input1 = Layer.InputLayer("l1", listOf(null, 1))
        val layer2 = Layer.UnknownLayer("l2", setOf(input1.name)).isTrainable()
        val layer3 =
            Layer.Dense("l3", setOf(input1.name), 10, Activation.SoftMax).isTrainable()

        val mockLayerToCode = mockk<LayerToCode> {
            every { makeNewLayer(Layer.InputLayer("l1", listOf(1)).layer) } returns "input1".right()
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
            oldModel = makeModel(
                input1 to layer2,
                input = setOf(input1),
                output = setOf(layer2)
            )
            newModel = makeModel(
                input1 to layer3,
                input = setOf(input1),
                output = setOf(layer3)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = input1
            |var2 = layer3(var1)
            |new_model = tf.keras.Model(inputs=[var1], outputs=[var2])
            |new_model.get_layer("l3").trainable = True
        """.trimMargin()

        verifyAll {
            mockLayerToCode.makeNewLayer(Layer.InputLayer("l1", listOf(1)).layer)
            mockLayerToCode.makeNewLayer(layer3.layer)
        }
        confirmVerified(mockLayerToCode)
    }

    @Test
    fun `replace a layer with an invalid layer`() {
        val input1 = Layer.InputLayer("l1", listOf(null, 1))
        val layer2 = Layer.UnknownLayer("l2", setOf(input1.name)).isTrainable()
        val layer3 = Layer.UnknownLayer("l3", setOf(input1.name)).isTrainable()

        val mockLayerToCode = mockk<LayerToCode> {
            every { makeNewLayer(Layer.InputLayer("l1", listOf(1)).layer) } returns "input1".right()
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
            oldModel =
                makeModel(input1 to layer2, input = setOf(input1), output = setOf(layer2))
            newModel = makeModel(input1 to layer3, input = setOf(input1), output = setOf(layer3))
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        shouldThrow<IllegalStateException> { task.code() }

        verifyAll {
            mockLayerToCode.makeNewLayer(Layer.InputLayer("l1", listOf(1)).layer)
            mockLayerToCode.makeNewLayer(layer3.layer)
        }
        confirmVerified(mockLayerToCode)
    }

    @Test
    fun `copy a layer with two inputs`() {
        val input1 = Layer.InputLayer("l1", listOf(null, 1))
        val layer2 = Layer.UnknownLayer("l2", setOf(input1.name)).isTrainable()
        val layer3 = Layer.Dense(
            "l3",
            setOf(input1.name, layer2.name),
            10,
            Activation.SoftMax
        ).isTrainable()

        val mockLayerToCode = mockk<LayerToCode> {
            every { makeNewLayer(Layer.InputLayer("l1", listOf(1)).layer) } returns "input1".right()
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
            oldModel = makeModel(
                input1 to layer2,
                input1 to layer3,
                layer2 to layer3,
                input = setOf(input1),
                output = setOf(layer3)
            )
            newModel = makeModel(
                input1 to layer2,
                input1 to layer3,
                layer2 to layer3,
                input = setOf(input1),
                output = setOf(layer3)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = input1
            |var2 = base_model.get_layer("l2")(var1)
            |var3 = base_model.get_layer("l3")([var1, var2])
            |new_model = tf.keras.Model(inputs=[var1], outputs=[var3])
            |new_model.get_layer("l2").trainable = True
            |new_model.get_layer("l3").trainable = True
        """.trimMargin()

        verifyAll { mockLayerToCode.makeNewLayer(Layer.InputLayer("l1", listOf(1)).layer) }
        confirmVerified(mockLayerToCode)
    }

    @Test
    fun `two model inputs are separated`() {
        val input1 = Layer.InputLayer("in1", listOf(null, 1))
        val input2 = Layer.InputLayer("in2", listOf(null, 1))
        val layer1 =
            Layer.UnknownLayer("l1", setOf(input1.name, input2.name)).isTrainable()

        val mockLayerToCode = mockk<LayerToCode> {
            every {
                makeNewLayer(
                    Layer.InputLayer(
                        "in1",
                        listOf(1)
                    ).layer
                )
            } returns "input1".right()
            every {
                makeNewLayer(
                    Layer.InputLayer(
                        "in2",
                        listOf(1)
                    ).layer
                )
            } returns "input2".right()
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
            oldModel = makeModel(
                input1 to layer1,
                input2 to layer1,
                input = setOf(input1, input2),
                output = setOf(layer1)
            )
            newModel = makeModel(
                input1 to layer1,
                input2 to layer1,
                input = setOf(input1, input2),
                output = setOf(layer1)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = input1
            |var2 = input2
            |var3 = base_model.get_layer("l1")([var1, var2])
            |new_model = tf.keras.Model(inputs=[var1, var2], outputs=[var3])
            |new_model.get_layer("l1").trainable = True
        """.trimMargin()

        verifyAll {
            mockLayerToCode.makeNewLayer(Layer.InputLayer("in1", listOf(1)).layer)
            mockLayerToCode.makeNewLayer(Layer.InputLayer("in2", listOf(1)).layer)
        }
        confirmVerified(mockLayerToCode)
    }

    @Test
    fun `swap two model inputs`() {
        val input1 = Layer.InputLayer("in1", listOf(null, 1))
        val input2 = Layer.InputLayer("in2", listOf(null, 1))
        val layer1 =
            Layer.UnknownLayer("l1", setOf(input1.name, input2.name)).isTrainable()

        val mockLayerToCode = mockk<LayerToCode> {
            every {
                makeNewLayer(
                    Layer.InputLayer(
                        "in1",
                        listOf(1)
                    ).layer
                )
            } returns "input1".right()
            every {
                makeNewLayer(
                    Layer.InputLayer(
                        "in2",
                        listOf(1)
                    ).layer
                )
            } returns "input2".right()
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
            oldModel = makeModel(
                input1 to layer1,
                input2 to layer1,
                input = setOf(input1, input2),
                output = setOf(layer1)
            )
            newModel = makeModel(
                input2 to layer1,
                input1 to layer1,
                input = setOf(input2, input1),
                output = setOf(layer1)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = input1
            |var2 = input2
            |var3 = base_model.get_layer("l1")([var1, var2])
            |new_model = tf.keras.Model(inputs=[var2, var1], outputs=[var3])
            |new_model.get_layer("l1").trainable = True
        """.trimMargin()

        verifyAll {
            mockLayerToCode.makeNewLayer(Layer.InputLayer("in1", listOf(1)).layer)
            mockLayerToCode.makeNewLayer(Layer.InputLayer("in2", listOf(1)).layer)
        }
        confirmVerified(mockLayerToCode)
    }

    @Test
    fun `reorder three model inputs`() {
        val input1 = Layer.InputLayer("in1", listOf(null, 1))
        val input2 = Layer.InputLayer("in2", listOf(null, 1))
        val input3 = Layer.InputLayer("in3", listOf(null, 1))
        val layer1 =
            Layer.UnknownLayer("l1", setOf(input1.name, input2.name)).isTrainable()

        val mockLayerToCode = mockk<LayerToCode> {
            every {
                makeNewLayer(
                    Layer.InputLayer(
                        "in1",
                        listOf(1)
                    ).layer
                )
            } returns "input1".right()
            every {
                makeNewLayer(
                    Layer.InputLayer(
                        "in2",
                        listOf(1)
                    ).layer
                )
            } returns "input2".right()
            every {
                makeNewLayer(
                    Layer.InputLayer(
                        "in3",
                        listOf(1)
                    ).layer
                )
            } returns "input3".right()
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
            oldModel = makeModel(
                input1 to layer1,
                input2 to layer1,
                input3 to layer1,
                input = setOf(input1, input2, input3),
                output = setOf(layer1)
            )
            newModel = makeModel(
                input2 to layer1,
                input3 to layer1,
                input1 to layer1,
                input = setOf(input2, input3, input1),
                output = setOf(layer1)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = input1
            |var2 = input2
            |var3 = input3
            |var4 = base_model.get_layer("l1")([var1, var2])
            |new_model = tf.keras.Model(inputs=[var2, var3, var1], outputs=[var4])
            |new_model.get_layer("l1").trainable = True
        """.trimMargin()

        verifyAll {
            mockLayerToCode.makeNewLayer(Layer.InputLayer("in1", listOf(1)).layer)
            mockLayerToCode.makeNewLayer(Layer.InputLayer("in2", listOf(1)).layer)
            mockLayerToCode.makeNewLayer(Layer.InputLayer("in3", listOf(1)).layer)
        }
        confirmVerified(mockLayerToCode)
    }

    @Test
    fun `two outputs where the output layers depend on each other`() {
        val input1 = Layer.InputLayer("in1", listOf(null, 1))
        val layer1 = Layer.UnknownLayer("l1", setOf(input1.name)).isTrainable()
        val layer2 = Layer.UnknownLayer("l2", setOf(layer1.name)).isTrainable()

        val mockLayerToCode = mockk<LayerToCode> {
            every {
                makeNewLayer(
                    Layer.InputLayer(
                        "in1",
                        listOf(1)
                    ).layer
                )
            } returns "input1".right()
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
            oldModel = makeModel(
                input1 to layer1,
                layer1 to layer2,
                input = setOf(input1),
                output = setOf(layer2)
            )
            newModel = makeModel(
                input1 to layer1,
                layer1 to layer2,
                input = setOf(input1),
                output = setOf(layer1, layer2)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = input1
            |var2 = base_model.get_layer("l1")(var1)
            |var3 = base_model.get_layer("l2")(var2)
            |new_model = tf.keras.Model(inputs=[var1], outputs=[var2, var3])
            |new_model.get_layer("l1").trainable = True
            |new_model.get_layer("l2").trainable = True
        """.trimMargin()

        verifyAll { mockLayerToCode.makeNewLayer(Layer.InputLayer("in1", listOf(1)).layer) }
        confirmVerified(mockLayerToCode)
    }

    @Test
    fun `two outputs where the output layers don't depend on each other`() {
        val input1 = Layer.InputLayer("in1", listOf(null, 1))
        val layer1 = Layer.UnknownLayer("l1", setOf(input1.name)).isTrainable()
        val layer2 = Layer.UnknownLayer("l2", setOf(input1.name)).isTrainable()

        val mockLayerToCode = mockk<LayerToCode> {
            every {
                makeNewLayer(
                    Layer.InputLayer(
                        "in1",
                        listOf(1)
                    ).layer
                )
            } returns "input1".right()
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
            oldModel = makeModel(
                input1 to layer1,
                input1 to layer2,
                input = setOf(input1),
                output = setOf(layer2)
            )
            newModel = makeModel(
                input1 to layer1,
                input1 to layer2,
                input = setOf(input1),
                output = setOf(layer1, layer2)
            )
            newModelOutput = configuredCorrectly("new_model")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code() shouldBe """
            |var1 = input1
            |var2 = base_model.get_layer("l1")(var1)
            |var3 = base_model.get_layer("l2")(var1)
            |new_model = tf.keras.Model(inputs=[var1], outputs=[var2, var3])
            |new_model.get_layer("l1").trainable = True
            |new_model.get_layer("l2").trainable = True
        """.trimMargin()

        verifyAll { mockLayerToCode.makeNewLayer(Layer.InputLayer("in1", listOf(1)).layer) }
        confirmVerified(mockLayerToCode)
    }
}
