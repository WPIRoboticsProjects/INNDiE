@file:SuppressWarnings("LongMethod", "LargeClass", "TooManyFunctions")

package edu.wpi.axon.tflayerloader

import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.SerializableEither
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Initializer
import edu.wpi.axon.tfdata.layer.Layer
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class LoadLayersWithInitializersIntegrationTest {

    @Test
    fun `load sequential with constant int initializer`() {
        loadModel<Model.Sequential>("sequential_with_constant_int_initializer.h5") {
            it.name shouldBe "sequential"
            it.batchInputShape shouldBe listOf(null, 1)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense",
                    null,
                    1,
                    Activation.Linear,
                    kernelInitializer = Initializer.Constant(SerializableEither.Left(0.0))
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with constant list initializer`() {
        loadModel<Model.Sequential>("sequential_with_constant_list_initializer.h5") {
            it.name shouldBe "sequential_2"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_2",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.Constant(SerializableEither.Right(listOf(1.0, 2.1)))
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with constant tuple initializer`() {
        loadModel<Model.Sequential>("sequential_with_constant_tuple_initializer.h5") {
            it.name shouldBe "sequential_3"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_3",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.Constant(SerializableEither.Right(listOf(1.0, 2.1)))
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with constant nparray initializer`() {
        loadModel<Model.Sequential>("sequential_with_constant_nparray_initializer.h5") {
            it.name shouldBe "sequential_4"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_4",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.Constant(SerializableEither.Right(listOf(1.0, 2.1)))
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with identity initializer`() {
        loadModel<Model.Sequential>("sequential_with_identity_initializer.h5") {
            it.name shouldBe "sequential_5"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_5",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.Identity(1.2)
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with orthogonal initializer`() {
        loadModel<Model.Sequential>("sequential_with_orthogonal_initializer.h5") {
            it.name shouldBe "sequential_6"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_6",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.Orthogonal(1.2, 3)
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with randomnormal initializer`() {
        loadModel<Model.Sequential>("sequential_with_randomnormal_initializer.h5") {
            it.name shouldBe "sequential_7"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_7",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.RandomNormal(1.0, 0.5, null)
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with randomuniform initializer`() {
        loadModel<Model.Sequential>("sequential_with_randomuniform_initializer.h5") {
            it.name shouldBe "sequential_2"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_2",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.RandomUniform(SerializableEither.Left(-0.1), SerializableEither.Left(0.1), null)
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with randomuniform tensor initializer`() {
        loadModel<Model.Sequential>("sequential_with_randomuniform_tensor_initializer.h5") {
            it.name shouldBe "sequential_3"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_3",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.RandomUniform(
                        SerializableEither.Right(listOf(-0.1, -0.2)),
                        SerializableEither.Right(listOf(0.1, 0.2)),
                        null
                    )
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with glorotnormal tensor initializer`() {
        loadModel<Model.Sequential>("sequential_with_glorotnormal_initializer.h5") {
            it.name shouldBe "sequential_4"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_4",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.GlorotNormal(null)
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with glorotuniform tensor initializer`() {
        loadModel<Model.Sequential>("sequential_with_glorotuniform_initializer.h5") {
            it.name shouldBe "sequential_5"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_5",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.GlorotUniform(null)
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with truncatednormal tensor initializer`() {
        loadModel<Model.Sequential>("sequential_with_truncatednormal_initializer.h5") {
            it.name shouldBe "sequential_6"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_6",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.TruncatedNormal(1.0, 2.0, null)
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with variancescaling fanin uniform tensor initializer`() {
        loadModel<Model.Sequential>("sequential_with_variancescaling_fanin_uniform_initializer.h5") {
            it.name shouldBe "sequential_8"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_8",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.VarianceScaling(
                        1.0,
                        Initializer.VarianceScaling.Mode.FanIn,
                        Initializer.VarianceScaling.Distribution.Uniform,
                        null
                    )
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with variancescaling fanout normal tensor initializer`() {
        loadModel<Model.Sequential>("sequential_with_variancescaling_fanout_normal_initializer.h5") {
            it.name shouldBe "sequential_9"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_9",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.VarianceScaling(
                        1.0,
                        Initializer.VarianceScaling.Mode.FanOut,
                        Initializer.VarianceScaling.Distribution.Normal,
                        null
                    )
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with variancescaling fanavg truncatednormal tensor initializer`() {
        loadModel<Model.Sequential>("sequential_with_variancescaling_fanavg_truncatednormal_initializer.h5") {
            it.name shouldBe "sequential_10"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_10",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.VarianceScaling(
                        1.0,
                        Initializer.VarianceScaling.Mode.FanAvg,
                        Initializer.VarianceScaling.Distribution.TruncatedNormal,
                        null
                    )
                ).trainable()
            )
        }
    }

    @Test
    fun `load sequential with variancescaling fanavg untruncatednormal tensor initializer`() {
        loadModel<Model.Sequential>("sequential_with_variancescaling_fanavg_untruncatednormal_initializer.h5") {
            it.name shouldBe "sequential_11"
            it.batchInputShape shouldBe listOf(null, 2)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_11",
                    null,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.VarianceScaling(
                        1.0,
                        Initializer.VarianceScaling.Mode.FanAvg,
                        Initializer.VarianceScaling.Distribution.UntruncatedNormal,
                        null
                    )
                ).trainable()
            )
        }
    }
}
