package edu.wpi.axon.plugin

import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldThrow
import java.io.File
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class LocalPluginManagerTest {

    @Test
    fun `list plugins with just official plugins`(@TempDir tempDir: File) {
        val officialPlugins = listOf(
            Plugin.Official("a"),
            Plugin.Official("b")
        )

        LocalPluginManager(tempDir, officialPlugins).apply { initialize() }
            .listPlugins()
            .shouldContainExactlyInAnyOrder(officialPlugins)
    }

    @Test
    fun `list plugins with just unofficial plugins`(@TempDir tempDir: File) {
        val manager = LocalPluginManager(tempDir, listOf())
        manager.initialize()
        val unofficialPlugins = listOf(
            Plugin.Unofficial("a"),
            Plugin.Unofficial("b")
        )
        unofficialPlugins.forEach { manager.addUnofficialPlugin(it) }
        manager.listPlugins().shouldContainExactlyInAnyOrder(unofficialPlugins)
    }

    @Test
    fun `list plugins with official and unofficial plugins`(@TempDir tempDir: File) {
        val officialPlugins = listOf(
            Plugin.Official("a"),
            Plugin.Official("b")
        )

        val unofficialPlugins = listOf(
            Plugin.Unofficial("a"),
            Plugin.Unofficial("b")
        )

        val manager = LocalPluginManager(tempDir, officialPlugins)
        manager.initialize()
        unofficialPlugins.forEach { manager.addUnofficialPlugin(it) }
        manager.listPlugins().shouldContainExactlyInAnyOrder(officialPlugins + unofficialPlugins)
    }

    @Test
    fun `list plugins with calling initialize`(@TempDir tempDir: File) {
        shouldThrow<IllegalStateException> { LocalPluginManager(tempDir, listOf()).listPlugins() }
    }

    @Test
    fun `load plugins from local cache`(@TempDir tempDir: File) {
        val pluginCache = LocalPluginManager.getPluginCacheFile(tempDir)
        val exampleCacheData = PluginCache(
            listOf(
                Plugin.Unofficial("a"),
                Plugin.Unofficial("b")
            )
        )

        pluginCache.writeText(exampleCacheData.serialize())

        val manager = LocalPluginManager(tempDir, listOf())
        manager.initialize()
        manager.listPlugins().shouldContainExactlyInAnyOrder(exampleCacheData.plugins)
    }

    @Test
    fun `adding a plugin adds it to the cache`(@TempDir tempDir: File) {
        val pluginCache = LocalPluginManager.getPluginCacheFile(tempDir)
        val plugin1 = Plugin.Unofficial("a")
        val plugin2 = Plugin.Unofficial("b")

        val exampleCacheData = PluginCache(listOf(plugin1))
        pluginCache.writeText(exampleCacheData.serialize())

        val manager = LocalPluginManager(tempDir, listOf())
        manager.initialize()
        manager.addUnofficialPlugin(plugin2)

        PluginCache.deserialize(pluginCache.readText()).plugins
            .shouldContainExactlyInAnyOrder(plugin1, plugin2)
    }

    @Test
    fun `removing a plugin removes it from the cache`(@TempDir tempDir: File) {
        val pluginCache = LocalPluginManager.getPluginCacheFile(tempDir)
        val plugin1 = Plugin.Unofficial("a")
        val plugin2 = Plugin.Unofficial("b")

        val exampleCacheData = PluginCache(listOf(plugin1, plugin2))
        pluginCache.writeText(exampleCacheData.serialize())

        val manager = LocalPluginManager(tempDir, listOf())
        manager.initialize()
        manager.removeUnofficialPlugin(plugin1)
        PluginCache.deserialize(pluginCache.readText()).plugins
            .shouldContainExactlyInAnyOrder(plugin2)
    }

    @Test
    fun `initialize with an invalid cache file`(@TempDir tempDir: File) {
        val pluginCache = LocalPluginManager.getPluginCacheFile(tempDir)
        pluginCache.writeText(RandomStringUtils.randomAlphanumeric(10))

        val manager = LocalPluginManager(tempDir, listOf())
        manager.initialize()
        manager.listPlugins().shouldBeEmpty()
    }
}
