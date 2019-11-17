package edu.wpi.axon.tfdata.code

import edu.wpi.axon.tfdata.Dataset
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class DefaultDatasetToCodeTest {

    private val datasetToCode = DefaultDatasetToCode()

    @Test
    fun `test code gen`() {
        val dataset = mockk<Dataset.ExampleDataset> { every { name } returns "dataset_name" }
        datasetToCode.datasetToCode(dataset) shouldBe "tf.keras.datasets.dataset_name"
    }
}
