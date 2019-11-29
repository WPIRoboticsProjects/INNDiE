package edu.wpi.axon.tfdata.layer

import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test

internal class LayerTest {

    @Test
    fun `dropout with valid rate`() {
        shouldNotThrow<IllegalArgumentException> { Layer.Dropout("", null, 0.5) }
    }

    @Test
    fun `dropout with invalid rate`() {
        shouldThrow<IllegalArgumentException> { Layer.Dropout("", null, -0.1) }
        shouldThrow<IllegalArgumentException> { Layer.Dropout("", null, 1.2) }
    }

    @Test
    fun `spatialdropout2d with valid rate`() {
        shouldNotThrow<IllegalArgumentException> { Layer.SpatialDropout2D("", null, 0.5) }
    }

    @Test
    fun `spatialdropout2d with invalid rate`() {
        shouldThrow<IllegalArgumentException> { Layer.SpatialDropout2D("", null, -0.1) }
        shouldThrow<IllegalArgumentException> { Layer.SpatialDropout2D("", null, 1.2) }
    }
}
