@file:SuppressWarnings("LongMethod", "LargeClass")
@file:Suppress("UnstableApiUsage")

package edu.wpi.inndie.tflayerloader

import edu.wpi.inndie.tfdata.Model
import edu.wpi.inndie.tfdata.SerializableEitherITii
import edu.wpi.inndie.tfdata.SerializableTuple2II
import edu.wpi.inndie.tfdata.layer.Activation
import edu.wpi.inndie.tfdata.layer.DataFormat
import edu.wpi.inndie.tfdata.layer.Layer
import edu.wpi.inndie.tfdata.layer.PoolingPadding
import io.kotlintest.matchers.booleans.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class HDF5ModelLoaderIntegrationTest {

    @Test
    fun `load mobilenetv2 exported using TF 1-15`() {
        loadModel<Model.General>("mobilenetv2_tf-1-15.h5") {
            it.name shouldBe "mobilenetv2_1.00_224"
            it.input.shouldContainExactly(
                Model.General.InputData("input_1", listOf(null, 224, 224, 3))
            )
            it.output.shouldContainExactly(Model.General.OutputData("Logits"))
            it.layers.nodes() shouldHaveSize 157

            val nodesWithMultipleInputs = it.layers.nodes().filter {
                it.inputs?.let { it.size > 1 } ?: false
            }

            // Only the block_xx_add layers should have more than one input
            nodesWithMultipleInputs.all {
                it.name.startsWith("block_") && it.name.endsWith("_add")
            }.shouldBeTrue()
        }
    }

    @Test
    fun `load mobilenetv2 exported using TF 1-14`() {
        loadModel<Model.General>("mobilenetv2_1.00_224.h5") {
            it.name shouldBe "mobilenetv2_1.00_224"
            it.input.shouldContainExactly(
                Model.General.InputData("input_1", listOf(null, 224, 224, 3))
            )
            it.output.shouldContainExactly(Model.General.OutputData("Logits"))
            it.layers.nodes() shouldHaveSize 157

            val nodesWithMultipleInputs = it.layers.nodes().filter {
                it.inputs?.let { it.size > 1 } ?: false
            }

            // Only the block_xx_add layers should have more than one input
            nodesWithMultipleInputs.all {
                it.name.startsWith("block_") && it.name.endsWith("_add")
            }.shouldBeTrue()
        }
    }

    @Test
    fun `load from test file 1`() {
        loadModel<Model.Sequential>("model1.h5") {
            it.name shouldBe "sequential_11"
            it.batchInputShape.shouldContainExactly(null, 28, 28, 1)
            it.layers.shouldContainExactly(
                setOf(
                    Layer.Conv2D(
                        "conv2d_16",
                        null,
                        32,
                        SerializableTuple2II(3, 3),
                        Activation.ReLu
                    ).isTrainable(),
                    Layer.Conv2D(
                        "conv2d_17",
                        null,
                        64,
                        SerializableTuple2II(3, 3),
                        Activation.ReLu
                    ).isTrainable(),
                    Layer.MaxPooling2D(
                        "max_pooling2d_8",
                        null,
                        SerializableEitherITii.Right(
                            SerializableTuple2II(2, 2)
                        ),
                        SerializableEitherITii.Right(
                            SerializableTuple2II(2, 2)
                        ),
                        PoolingPadding.Valid,
                        DataFormat.ChannelsLast
                    ).isTrainable(),
                    Layer.Dropout(
                        "dropout_19",
                        null,
                        0.25
                    ).isTrainable(),
                    Layer.Flatten(
                        "flatten_8",
                        null,
                        DataFormat.ChannelsLast
                    ).isTrainable(),
                    Layer.Dense(
                        "dense_22",
                        null,
                        128,
                        Activation.ReLu
                    ).isTrainable(),
                    Layer.Dropout(
                        "dropout_20",
                        null,
                        0.5
                    ).isTrainable(),
                    Layer.Dense(
                        "dense_23",
                        null,
                        10,
                        Activation.SoftMax
                    ).isTrainable()
                )
            )
        }
    }

    @Test
    fun `load from bad file`() {
        loadModelFails("badModel1.h5")
    }

    @Test
    fun `load non-sequential model 1`() {
        val layers = setOf(
            Layer.InputLayer("input_2", listOf(null, 3)),
            Layer.Dense(
                "dense_2",
                setOf("input_2"),
                4,
                Activation.ReLu
            ).isTrainable(), Layer.Dense(
                "dense_3",
                setOf("dense_2"),
                5,
                Activation.SoftMax
            ).isTrainable()
        )

        loadModel<Model.General>("nonSequentialModel1.h5") {
            it.name shouldBe "model_1"
            it.input.shouldContainExactly(Model.General.InputData("input_2", listOf(null, 3)))
            it.output.shouldContainExactly(Model.General.OutputData("dense_3"))
            it.layers.nodes() shouldContainExactlyInAnyOrder layers
        }
    }

    @Test
    fun `load rnn 1`() {
        loadModel<Model.General>("rnn1.h5") {
            it.name shouldBe "model_5"
            it.input.shouldContainExactly(
                Model.General.InputData(
                    "input_15",
                    listOf(null, null, 5)
                )
            )
            it.output.shouldContainExactly(Model.General.OutputData("dense_1"))
            it.layers.nodes().shouldContainExactly(
                // TODO: Add an RNN layer class
                Layer.InputLayer("input_15", listOf(null, null, 5)),
                Layer.UnknownLayer("rnn_12", setOf("input_15")).isTrainable(),
                Layer.Dense(
                    "dense_1",
                    setOf("rnn_12"),
                    10,
                    Activation.SoftMax
                ).isTrainable()
            )
        }
    }
}
