package edu.wpi.axon.training

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * The path to a model.
 */
@Serializable
sealed class ModelPath {

    /**
     * The full pathname of the model.
     */
    abstract val path: String

    /**
     * The filename of the model.
     */
    val filename: String
        get() = path.substringAfterLast("/")

    /**
     * A path in S3. This does not include any prefixes that Axon uses to sort files.
     */
    @Serializable
    data class S3(override val path: String) : ModelPath()

    /**
     * A path on the local disk.
     */
    @Serializable
    data class Local(override val path: String) : ModelPath()

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
