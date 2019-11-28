package edu.wpi.axon.tfdata.code

import edu.wpi.axon.tfdata.Dataset
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class DefaultExampleDatasetToCodeTest {

    private val datasetToCode = DefaultExampleDatasetToCode()

    @Test
    fun `test example dataset`() {
        val dataset = mockk<Dataset.ExampleDataset> { every { name } returns "dataset_name" }
        datasetToCode.datasetToCode(dataset) shouldBe "tf.keras.datasets.dataset_name"
    }
}
