package edu.wpi.inndie.examplemodel

import edu.wpi.inndie.tfdata.SerializableOptionB
import kotlinx.serialization.Serializable

/**
 * One of the models that Axon supports out of the box.
 */
@Serializable
data class ExampleModel(
    /**
     * The pretty name of the model.
     */
    var name: String,
    /**
     * The filename the model should be put in.
     */
    var fileName: String,
    /**
     * The url to the model.
     */
    var url: String,
    /**
     * A short description of what the model does.
     */
    var description: String,
    /**
     * Which layers to freeze by default. Associates a layer name with an optional trainable
     * flag.
     */
    var freezeLayers: Map<String, SerializableOptionB>
)
