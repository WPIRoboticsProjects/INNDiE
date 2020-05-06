package edu.wpi.inndie.examplemodel

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

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
