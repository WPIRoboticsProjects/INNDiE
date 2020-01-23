package edu.wpi.axon.tflayerloader

class ModelLoaderFactory {

    /**
     * Creates a new [ModelLoader] based on the extension of the [modelFilename].
     *
     * @param modelFilename The filename of the model file that is going to be loaded.
     * @return A new [ModelLoader].
     */
    fun createModeLoader(modelFilename: String): ModelLoader = when {
        modelFilename.endsWith(".h5") || modelFilename.endsWith(".hdf5") ->
            HDF5ModelLoader(DefaultLayersToGraph())
        else -> error("Model file type not supported for model with filename: $modelFilename")
    }
}
