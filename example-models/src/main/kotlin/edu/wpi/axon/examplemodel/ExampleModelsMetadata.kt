package edu.wpi.axon.examplemodel

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * The main file (named `exampleModels.json` in the repo) which specifies all the metadata about
 * the example models.
 */
@Serializable
data class ExampleModelsMetadata(
    val exampleModels: Set<ExampleModel>
) {

    companion object {
        fun deserialize(data: String) = Json(JsonConfiguration.Stable).parse(serializer(), data)
    }
}
