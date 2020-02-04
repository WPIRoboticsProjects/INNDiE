package edu.wpi.axon.plugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * A plugin. Plugins are scripts that Axon includes in code generation for various purposes.
 */
@Serializable
sealed class Plugin {

    /**
     * The name of this plugin.
     */
    abstract val name: String

    /**
     * The contents of the plugin to be included in the generated code.
     */
    abstract val contents: String

    /**
     * An Axon-supported plugin that users get by default.
     */
    @Serializable
    data class Official(
        override val name: String,
        override val contents: String
    ) : Plugin()

    /**
     * A plugin that the user added themselves.
     */
    @Serializable
    data class Unofficial(
        override val name: String,
        override val contents: String
    ) : Plugin()

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
