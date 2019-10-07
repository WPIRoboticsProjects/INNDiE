package edu.wpi.axon.tflayerloader

import arrow.core.Left
import arrow.core.None
import arrow.core.Right
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Initializer
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tfdata.layer.trainable
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
                SealedLayer.Dense(
                    "dense",
                    None,
                    1,
                    Activation.Linear,
                    kernelInitializer = Initializer.Constant(Left(0.0))
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
                SealedLayer.Dense(
                    "dense_2",
                    None,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.Constant(Right(listOf(1.0, 2.1)))
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
                SealedLayer.Dense(
                    "dense_3",
                    None,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.Constant(Right(listOf(1.0, 2.1)))
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
                SealedLayer.Dense(
                    "dense_4",
                    None,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.Constant(Right(listOf(1.0, 2.1)))
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
                SealedLayer.Dense(
                    "dense_5",
                    None,
                    2,
                    Activation.Linear,
                    kernelInitializer = Initializer.Identity(1.2)
                ).trainable()
            )
        }
    }
}
