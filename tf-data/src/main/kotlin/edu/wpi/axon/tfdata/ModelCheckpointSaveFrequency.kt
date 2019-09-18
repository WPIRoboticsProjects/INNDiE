package edu.wpi.axon.tfdata

sealed class ModelCheckpointSaveFrequency {

    /**
     * Checkpoint after each epoch.
     */
    object Epoch : ModelCheckpointSaveFrequency() {
        override fun toString() = """"epoch""""
    }

    /**
     * Checkpoint at the end of a batch at which [numSamplesPerCheckpoint] number of samples have
     * been seen since the last checkpoint. This can produce less reliable metrics than [Epoch].
     */
    data class Samples(val numSamplesPerCheckpoint: Int) : ModelCheckpointSaveFrequency() {
        override fun toString() = numSamplesPerCheckpoint.toString()
    }
}
