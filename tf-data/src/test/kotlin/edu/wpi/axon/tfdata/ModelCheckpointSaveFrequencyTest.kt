package edu.wpi.axon.tfdata

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class ModelCheckpointSaveFrequencyTest {

    @Test
    fun `test epoch`() {
        ModelCheckpointSaveFrequency.Epoch.toString() shouldBe """"epoch""""
    }

    @Test
    fun `test integer`() {
        ModelCheckpointSaveFrequency.Samples(10).toString() shouldBe "10"
    }
}
