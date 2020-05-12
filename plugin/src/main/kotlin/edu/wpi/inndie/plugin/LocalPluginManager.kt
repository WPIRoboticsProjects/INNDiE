package edu.wpi.inndie.plugin

import java.io.File
import kotlinx.serialization.json.JsonDecodingException
import mu.KotlinLogging

/**
 * Saves plugin to the local filesystem.
 *
 * @param pluginCacheFile The plugin save file.
 * @param officialPlugins The INNDiE-supported official plugins that should always be available to the
 * user.
 */
class LocalPluginManager(
    private val pluginCacheFile: File,
    private val officialPlugins: Set<Plugin.Official>
) : PluginManager {

    private var initialized = false
    private val unofficialPlugins = mutableSetOf<Plugin.Unofficial>()

    override fun initialize() {
        if (!pluginCacheFile.exists()) {
            pluginCacheFile.createNewFile()
        }

        val cache = try {
            PluginCache.deserialize(
                pluginCacheFile.readText()
            )
        } catch (ex: JsonDecodingException) {
            LOGGER.warn(ex) { "Invalid plugin cache file contents. Creating new cache." }
            val newCache = PluginCache(setOf())
            pluginCacheFile.writeText(newCache.serialize())
            newCache
        }

        unofficialPlugins.addAll(cache.plugins)
        initialized = true
    }

    override fun listPlugins(): Set<Plugin> {
        check(initialized)
        return officialPlugins + unofficialPlugins
    }

    override fun addUnofficialPlugin(plugin: Plugin.Unofficial) {
        check(initialized)
        unofficialPlugins.add(plugin)
        synchronizeCacheFile()
    }

    override fun removeUnofficialPlugin(pluginName: String) {
        check(initialized)
        unofficialPlugins.remove(unofficialPlugins.first { it.name == pluginName })
        synchronizeCacheFile()
    }

    override fun modifyUnofficialPlugin(pluginName: String, plugin: Plugin.Unofficial) {
        check(initialized)
        unofficialPlugins.remove(unofficialPlugins.first { it.name == pluginName })
        unofficialPlugins.add(plugin)
        synchronizeCacheFile()
    }

    private fun synchronizeCacheFile() {
        pluginCacheFile.writeText(
            PluginCache(
                unofficialPlugins
            ).serialize())
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
