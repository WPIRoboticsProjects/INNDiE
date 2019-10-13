package edu.wpi.axon.tfdata.layer

import arrow.core.None
import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test

internal class LayerTest {

    @Test
    fun `dropout with valid rate`() {
        shouldNotThrow<IllegalArgumentException> { Layer.Dropout("", None, 0.5) }
    }

    @Test
    fun `dropout with invalid rate`() {
        shouldThrow<IllegalArgumentException> { Layer.Dropout("", None, 1.2) }
    }
}
