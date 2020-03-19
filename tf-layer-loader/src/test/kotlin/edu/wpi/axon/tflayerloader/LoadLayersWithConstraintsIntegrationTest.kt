package edu.wpi.axon.tflayerloader

import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Constraint
import edu.wpi.axon.tfdata.layer.Layer
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class LoadLayersWithConstraintsIntegrationTest {

    @Test
    fun `load sequential with maxnorm constraint`() {
        loadModel<Model.Sequential>("sequential_with_maxnorm_constraint.h5") {
            it.name shouldBe "sequential_12"
            it.batchInputShape shouldBe listOf(null, 1)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_6",
                    null,
                    1,
                    Activation.Linear,
                    kernelConstraint = Constraint.MaxNorm(2.0, 0)
                ).isTrainable()
            )
        }
    }

    @Test
    fun `load sequential with minmaxnorm constraint`() {
        loadModel<Model.Sequential>("sequential_with_minmaxnorm_constraint.h5") {
            it.name shouldBe "sequential_13"
            it.batchInputShape shouldBe listOf(null, 1)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_7",
                    null,
                    1,
                    Activation.Linear,
                    kernelConstraint = Constraint.MinMaxNorm(1.0, 2.0, 3.0, 0)
                ).isTrainable()
            )
        }
    }

    @Test
    fun `load sequential with unitnorm constraint`() {
        loadModel<Model.Sequential>("sequential_with_unitnorm_constraint.h5") {
            it.name shouldBe "sequential_15"
            it.batchInputShape shouldBe listOf(null, 1)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_9",
                    null,
                    1,
                    Activation.Linear,
                    kernelConstraint = Constraint.UnitNorm(0)
                ).isTrainable()
            )
        }
    }

    @Test
    fun `load sequential with nonneg constraint`() {
        loadModel<Model.Sequential>("sequential_with_nonneg_constraint.h5") {
            it.name shouldBe "sequential_14"
            it.batchInputShape shouldBe listOf(null, 1)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_8",
                    null,
                    1,
                    Activation.Linear,
                    kernelConstraint = Constraint.NonNeg
                ).isTrainable()
            )
        }
    }
}
