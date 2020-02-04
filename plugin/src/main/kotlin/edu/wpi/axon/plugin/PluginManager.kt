package edu.wpi.axon.plugin

/**
 * Manages saving/loading plugins to/from persistent storage.
 */
interface PluginManager {

    /**
     * Initializes the plugin manager. Loads plugins from storage. This must be called before any of
     * the other methods.
     */
    fun initialize()

    /**
     * Lists all the plugins.
     *
     * @return All plugins.
     */
    fun listPlugins(): Set<Plugin>

    /**
     * Adds a plugin.
     *
     * @param plugin The plugin to add.
     */
    fun addUnofficialPlugin(plugin: Plugin.Unofficial)

    /**
     * Removes a plugin.
     *
     * @param plugin The the plugin to remove.
     */
    fun removeUnofficialPlugin(plugin: Plugin.Unofficial)

    /**
     * Modified a plugin.
     *
     * @param pluginName The name of the plugin to modify.
     * @param plugin The new plugin value.
     */
    fun modifyUnofficialPlugin(pluginName: String, plugin: Plugin.Unofficial)
}
