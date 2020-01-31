package edu.wpi.axon.plugin

import kotlinx.serialization.Serializable

/**
 * A plugin. Plugins are scripts that Axon includes in code generation for various purposes.
 */
@Serializable
sealed class Plugin {

    /**
     * The contents of the plugin to be included in the generated code.
     */
    abstract val contents: String

    /**
     * An Axon-supported plugin that users get by default.
     */
    @Serializable
    data class Official(override val contents: String) : Plugin()

    /**
     * A plugin that the user added themselves.
     */
    @Serializable
    data class Unofficial(override val contents: String) : Plugin()
}
