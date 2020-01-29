package edu.wpi.axon.training

/**
 * All the ways a model can be deployed. These affect how the model is trained.
 */
sealed class ModelDeploymentTarget {

    /**
     * The model is going to be deployed to a desktop-class machine that can load the standard
     * HDF5 file output.
     */
    object Normal : ModelDeploymentTarget()

    /**
     * The model is going to be deployed to a mobile system that uses the Coral Edge TPU.
     */
    data class Coral(val representativeDatasetPercentage: Double) : ModelDeploymentTarget()
}
