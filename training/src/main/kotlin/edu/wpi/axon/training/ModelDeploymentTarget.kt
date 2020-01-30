package edu.wpi.axon.training

/**
 * All the ways a model can be deployed. These affect how the model is trained.
 */
sealed class ModelDeploymentTarget {

    /**
     * The model is going to be deployed to a desktop-class machine that can load the standard
     * HDF5 file output.
     */
    object Desktop : ModelDeploymentTarget()

    /**
     * The model is going to be deployed to a mobile system that uses the Coral Edge TPU.
     *
     * @param representativeDatasetPercentage The percentage of the training dataset to use for the
     * representative dataset used for post-training quantization.
     */
    data class Coral(val representativeDatasetPercentage: Double) : ModelDeploymentTarget()
}
