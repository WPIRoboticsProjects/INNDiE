package edu.wpi.axon.examplemodel

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
     * The url to the model.
     */
    val url: String,
    /**
     * A short description of what the model does.
     */
    val description: String,
    /**
     * Which layers to freeze by default.
     */
    val freezeLayers: List<Pair<Int, Int>>
)
