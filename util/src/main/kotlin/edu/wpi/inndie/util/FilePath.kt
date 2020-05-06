package edu.wpi.inndie.util

import java.nio.file.Paths
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * The path to a resource (model, dataset, etc.).
 */
@Serializable
sealed class FilePath {

    /**
     * The full pathname.
     */
    abstract val path: String

    /**
     * The filename.
     */
    val filename: String
        get() = Paths.get(path).fileName.toString()

    /**
     * A path in S3. This does not include any prefixes that INNDiE uses to sort files.
     */
    @Serializable
    data class S3(override val path: String) : FilePath()

    /**
     * A path on the local disk.
     */
    @Serializable
    data class Local(override val path: String) : FilePath()

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
