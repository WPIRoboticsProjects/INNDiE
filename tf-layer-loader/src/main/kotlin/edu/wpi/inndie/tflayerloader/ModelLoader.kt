package edu.wpi.inndie.tflayerloader

import arrow.fx.IO
import edu.wpi.axon.tfdata.Model
import java.io.File

interface ModelLoader {

    /**
     * Load a [Model] from the [file].
     *
     * @param file The file to load from.
     * @return A new [Model] containing as much of the information from the [file] as possible.
     */
    fun load(file: File): IO<Model>
}
