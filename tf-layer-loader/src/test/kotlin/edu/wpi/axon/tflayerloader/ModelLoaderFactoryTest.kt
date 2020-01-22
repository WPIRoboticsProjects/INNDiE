package edu.wpi.axon.tflayerloader

import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ModelLoaderFactoryTest {

    private val factory = ModelLoaderFactory()

    @Test
    fun `test h5 file`() {
        assertTrue(factory.createModeLoader("a.h5") is LoadLayersFromHDF5)
    }

    @Test
    fun `test hdf5 file`() {
        assertTrue(factory.createModeLoader("a.hdf5") is LoadLayersFromHDF5)
    }

    @Test
    fun `test unknown file`() {
        assertThrows<IllegalStateException> {
            factory.createModeLoader("a.abcd")
        }
    }
}
