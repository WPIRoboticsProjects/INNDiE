package edu.wpi.axon.plugin

import java.io.File
import kotlinx.serialization.json.JsonDecodingException
import mu.KotlinLogging

/**
 * Saves plugin to the local filesystem.
 *
 * @param cacheDir The plugin save file will be made in this directory.
 * @param officialPlugins The Axon-supported official plugins that should always be available to the
 * user.
 */
class LocalPluginManager(
    private val cacheDir: File,
    private val officialPlugins: List<Plugin.Official>
) : PluginManager {

    private var initialized = false
    private lateinit var pluginCacheFile: File
    private val unofficialPlugins = mutableListOf<Plugin.Unofficial>()

    override fun initialize() {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        pluginCacheFile = getPluginCacheFile(cacheDir)
        if (!pluginCacheFile.exists()) {
            pluginCacheFile.createNewFile()
        }

        val cache = try {
            PluginCache.deserialize(pluginCacheFile.readText())
        } catch (ex: JsonDecodingException) {
            LOGGER.warn(ex) { "Invalid plugin cache file contents. Creating new cache." }
            val newCache = PluginCache(listOf())
            pluginCacheFile.writeText(newCache.serialize())
            newCache
        }

        unofficialPlugins.addAll(cache.plugins)
        initialized = true
    }

    override fun listPlugins(): List<Plugin> {
        check(initialized)
        return officialPlugins + unofficialPlugins
    }

    override fun addUnofficialPlugin(plugin: Plugin.Unofficial) {
        check(initialized)
        unofficialPlugins.add(plugin)
        synchronizeCacheFile()
    }

    override fun removeUnofficialPlugin(plugin: Plugin.Unofficial) {
        check(initialized)
        unofficialPlugins.remove(plugin)
        synchronizeCacheFile()
    }

    private fun synchronizeCacheFile() {
        pluginCacheFile.writeText(PluginCache(unofficialPlugins).serialize())
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }

        fun getPluginCacheFile(cacheDir: File) = File(cacheDir, "plugin_cache.json")
    }
}
