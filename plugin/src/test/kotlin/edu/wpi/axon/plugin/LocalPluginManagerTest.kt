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
        val pluginCacheFile = File(tempDir, "cache.json").apply { createNewFile() }
        val officialPlugins = listOf(
            Plugin.Official("a", "a"),
            Plugin.Official("b", "b")
        )

        LocalPluginManager(pluginCacheFile, officialPlugins).apply { initialize() }
            .listPlugins()
            .shouldContainExactlyInAnyOrder(officialPlugins)
    }

    @Test
    fun `list plugins with just unofficial plugins`(@TempDir tempDir: File) {
        val pluginCacheFile = File(tempDir, "cache.json").apply { createNewFile() }
        val manager = LocalPluginManager(pluginCacheFile, listOf())
        manager.initialize()
        val unofficialPlugins = listOf(
            Plugin.Unofficial("a", "a"),
            Plugin.Unofficial("b", "b")
        )
        unofficialPlugins.forEach { manager.addUnofficialPlugin(it) }
        manager.listPlugins().shouldContainExactlyInAnyOrder(unofficialPlugins)
    }

    @Test
    fun `list plugins with official and unofficial plugins`(@TempDir tempDir: File) {
        val pluginCacheFile = File(tempDir, "cache.json").apply { createNewFile() }
        val officialPlugins = listOf(
            Plugin.Official("a", "a"),
            Plugin.Official("b", "b")
        )

        val unofficialPlugins = listOf(
            Plugin.Unofficial("a", "a"),
            Plugin.Unofficial("b", "b")
        )

        val manager = LocalPluginManager(pluginCacheFile, officialPlugins)
        manager.initialize()
        unofficialPlugins.forEach { manager.addUnofficialPlugin(it) }
        manager.listPlugins().shouldContainExactlyInAnyOrder(officialPlugins + unofficialPlugins)
    }

    @Test
    fun `list plugins with calling initialize`(@TempDir tempDir: File) {
        val pluginCacheFile = File(tempDir, "cache.json").apply { createNewFile() }
        shouldThrow<IllegalStateException> { LocalPluginManager(tempDir, listOf()).listPlugins() }
    }

    @Test
    fun `load plugins from local cache`(@TempDir tempDir: File) {
        val pluginCacheFile = File(tempDir, "cache.json").apply { createNewFile() }
        val exampleCacheData = PluginCache(
            listOf(
                Plugin.Unofficial("a", "a"),
                Plugin.Unofficial("b", "b")
            )
        )

        pluginCacheFile.writeText(exampleCacheData.serialize())

        val manager = LocalPluginManager(pluginCacheFile, listOf())
        manager.initialize()
        manager.listPlugins().shouldContainExactlyInAnyOrder(exampleCacheData.plugins)
    }

    @Test
    fun `adding a plugin adds it to the cache`(@TempDir tempDir: File) {
        val pluginCacheFile = File(tempDir, "cache.json").apply { createNewFile() }
        val plugin1 = Plugin.Unofficial("a", "a")
        val plugin2 = Plugin.Unofficial("b", "b")

        val exampleCacheData = PluginCache(listOf(plugin1))
        pluginCacheFile.writeText(exampleCacheData.serialize())

        val manager = LocalPluginManager(pluginCacheFile, listOf())
        manager.initialize()
        manager.addUnofficialPlugin(plugin2)

        PluginCache.deserialize(pluginCacheFile.readText()).plugins
            .shouldContainExactlyInAnyOrder(plugin1, plugin2)
    }

    @Test
    fun `removing a plugin removes it from the cache`(@TempDir tempDir: File) {
        val pluginCacheFile = File(tempDir, "cache.json").apply { createNewFile() }
        val plugin1 = Plugin.Unofficial("a", "a")
        val plugin2 = Plugin.Unofficial("b", "b")

        val exampleCacheData = PluginCache(listOf(plugin1, plugin2))
        pluginCacheFile.writeText(exampleCacheData.serialize())

        val manager = LocalPluginManager(pluginCacheFile, listOf())
        manager.initialize()
        manager.removeUnofficialPlugin(plugin1)
        PluginCache.deserialize(pluginCacheFile.readText()).plugins
            .shouldContainExactlyInAnyOrder(plugin2)
    }

    @Test
    fun `initialize with an invalid cache file`(@TempDir tempDir: File) {
        val pluginCacheFile = File(tempDir, "cache.json").apply { createNewFile() }
        pluginCacheFile.writeText(RandomStringUtils.randomAlphanumeric(10))

        val manager = LocalPluginManager(pluginCacheFile, listOf())
        manager.initialize()
        manager.listPlugins().shouldBeEmpty()
    }
}
