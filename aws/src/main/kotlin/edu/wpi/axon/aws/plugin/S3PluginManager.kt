package edu.wpi.axon.aws.plugin

import edu.wpi.axon.aws.S3Manager
import edu.wpi.axon.plugin.LocalPluginManager
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.plugin.PluginManager
import mu.KotlinLogging
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.io.File
import java.nio.file.Files

/**
 * Saves plugins to S3.
 *
 * @param s3Manager Used for interacting with S3.
 * @param cacheName The name of the plugin cache.
 * @param officialPlugins The Axon-supported official plugins that should always be available to the
 * user.
 */
class S3PluginManager(
    private val s3Manager: S3Manager,
    private val cacheName: String,
    private val officialPlugins: List<Plugin.Official>
) : PluginManager {

    private lateinit var localPluginManager: LocalPluginManager
    private lateinit var cacheFile: File

    override fun initialize() {
        cacheFile = try {
            s3Manager.downloadPluginCache(cacheName)
        } catch (ex: NoSuchKeyException) {
            LOGGER.warn(ex) { "Failed to download plugin cache from S3. Creating a new one." }
            Files.createTempFile("", ".json").toFile().apply { createNewFile() }
        }
        localPluginManager = LocalPluginManager(cacheFile, officialPlugins)
        localPluginManager.initialize()
    }

    override fun listPlugins() = localPluginManager.listPlugins()

    override fun addUnofficialPlugin(plugin: Plugin.Unofficial) {
        localPluginManager.addUnofficialPlugin(plugin)
        s3Manager.uploadPluginCache(cacheName, cacheFile)
    }

    override fun removeUnofficialPlugin(plugin: Plugin.Unofficial) {
        localPluginManager.removeUnofficialPlugin(plugin)
        s3Manager.uploadPluginCache(cacheName, cacheFile)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
