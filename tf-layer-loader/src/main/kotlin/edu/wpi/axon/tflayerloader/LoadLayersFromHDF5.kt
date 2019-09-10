package edu.wpi.axon.tflayerloader

import edu.wpi.axon.tflayers.Layer
import io.jhdf.HdfFile
import java.io.File

class LoadLayersFromHDF5 {

    fun load(file: File): List<Layer> {
        HdfFile(file).use {
            val config = it.getAttribute("model_config").data
            return listOf()
        }
    }
}
