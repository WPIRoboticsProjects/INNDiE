package edu.wpi.axon.db.data

import edu.wpi.axon.examplemodel.ExampleModel
import edu.wpi.axon.util.FilePath
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * The various sources a model could be loaded from.
 */
@Serializable
sealed class ModelSource {

    /**
     * From an example model.
     */
    @Serializable
    data class FromExample(val exampleModel: ExampleModel) : ModelSource()

    /**
     * From a FilePath.
     */
    @Serializable
    data class FromFile(val filePath: FilePath) : ModelSource()

    /**
     * From the trained output of a Job.
     */
    @Serializable
    data class FromJob(val jobId: Int) : ModelSource()

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
