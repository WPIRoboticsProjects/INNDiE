package edu.wpi.axon.plugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * A cache of unofficial plugins.
 *
 * @param plugins The plugins in the cache.
 */
@Serializable
data class PluginCache(
    val plugins: Set<Plugin.Unofficial>
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
