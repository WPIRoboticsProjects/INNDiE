package edu.wpi.inndie.tflayerloader

import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.layer.Regularizer
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class LoadLayersWithRegularizersIntegrationTest {

    @Test
    fun `load sequential with l1 regularizer`() {
        loadModel<Model.Sequential>("sequential_with_l1_regularizer.h5") {
            it.name shouldBe "sequential_9"
            it.batchInputShape shouldBe listOf(null, 1)
            it.layers.shouldContainExactly(
                Layer.Dense(
                    "dense_3",
                    null,
                    1,
                    Activation.Linear,
                    kernelRegularizer = Regularizer.L1L2(0.01, 0.0)
                ).isTrainable()
            )
        }
    }
}
