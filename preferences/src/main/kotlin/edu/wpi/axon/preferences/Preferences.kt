package edu.wpi.axon.preferences

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * The user's preferences.
 *
 * @param defaultEC2NodeType The default EC2 node type to run training scripts in.
 */
@Serializable
data class Preferences(
    val defaultEC2NodeType: String = "t2.micro"
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
