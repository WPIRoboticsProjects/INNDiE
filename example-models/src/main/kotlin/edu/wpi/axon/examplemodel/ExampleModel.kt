package edu.wpi.axon.examplemodel

import edu.wpi.axon.tfdata.SerializableOptionB
import kotlinx.serialization.Serializable

/**
 * One of the models that Axon supports out of the box.
 */
@Serializable
data class ExampleModel(
    /**
     * The pretty name of the model.
     */
    val name: String,
    /**
     * The filename the model should be put in.
     */
    val fileName: String,
    /**
     * The url to the model.
     */
    val url: String,
    /**
     * A short description of what the model does.
     */
    val description: String,
    /**
     * Which layers to freeze by default. Associated a layer name with an optional trainable
     * flag.
     */
    val freezeLayers: Map<String, SerializableOptionB>
)
