package edu.wpi.axon.tflayerloader

import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ModelLoaderFactoryTest {

    private val factory = ModelLoaderFactory()

    @Test
    fun `test h5 file`() {
        assertTrue(factory.createModelLoader("a.h5") is HDF5ModelLoader)
    }

    @Test
    fun `test hdf5 file`() {
        assertTrue(factory.createModelLoader("a.hdf5") is HDF5ModelLoader)
    }

    @Test
    fun `test unknown file`() {
        assertThrows<IllegalStateException> {
            factory.createModelLoader("a.abcd")
        }
    }
}
