package edu.wpi.inndie.tfdata.code

import edu.wpi.axon.tfdata.Dataset
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class DefaultExampleDatasetToCodeTest {

    private val datasetToCode = DefaultExampleDatasetToCode()

    @Test
    fun `test fashion mnist`() {
        datasetToCode.datasetToCode(Dataset.ExampleDataset.FashionMnist) shouldBe
            "    return tf.keras.datasets.fashion_mnist.load_data()"
    }
}
