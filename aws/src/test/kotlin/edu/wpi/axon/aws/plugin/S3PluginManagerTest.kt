package edu.wpi.axon.aws.plugin

import edu.wpi.axon.aws.S3Manager
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.plugin.PluginCache
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class S3PluginManagerTest {

    @Test
    fun `download plugins from s3`(@TempDir tempDir: File) {
        val pluginCacheFile = File(tempDir, "cache.json").apply { createNewFile() }
        val plugin1 = Plugin.Unofficial("a", "a")
        val plugin2 = Plugin.Unofficial("b", "b")
        pluginCacheFile.writeText(PluginCache(listOf(plugin1, plugin2)).serialize())

        val s3Manager = mockk<S3Manager> {
            every { downloadPluginCache(any()) } returns pluginCacheFile
        }

        val manager = S3PluginManager(
            s3Manager,
            "plugin-cache",
            listOf()
        )
        manager.initialize()
        manager.listPlugins().shouldContainExactlyInAnyOrder(plugin1, plugin2)

        verify(exactly = 1) { s3Manager.downloadPluginCache("plugin-cache") }
    }

    @Test
    fun `adding a plugin adds it to s3`(@TempDir tempDir: File) {
        val pluginCacheFile = File(tempDir, "cache.json").apply { createNewFile() }
        val plugin1 = Plugin.Unofficial("a", "a")
        val s3Manager = mockk<S3Manager> {
            every { downloadPluginCache(any()) } returns pluginCacheFile
            every { uploadPluginCache(any(), any()) } returns Unit
        }

        val manager = S3PluginManager(
            s3Manager,
            "plugin-cache",
            listOf()
        )
        manager.initialize()
        manager.addUnofficialPlugin(plugin1)
        manager.listPlugins().shouldContainExactlyInAnyOrder(plugin1)

        verify(exactly = 1) { s3Manager.downloadPluginCache("plugin-cache") }
        verify(exactly = 1) { s3Manager.uploadPluginCache("plugin-cache", pluginCacheFile) }
    }

    @Test
    fun `removing a plugin removes it from s3`(@TempDir tempDir: File) {
        val pluginCacheFile = File(tempDir, "cache.json").apply { createNewFile() }
        val plugin1 = Plugin.Unofficial("a", "a")
        val plugin2 = Plugin.Unofficial("b", "b")
        pluginCacheFile.writeText(PluginCache(listOf(plugin1, plugin2)).serialize())

        val s3Manager = mockk<S3Manager> {
            every { downloadPluginCache(any()) } returns pluginCacheFile
            every { uploadPluginCache(any(), any()) } returns Unit
        }

        val manager = S3PluginManager(
            s3Manager,
            "plugin-cache",
            listOf()
        )
        manager.initialize()
        manager.removeUnofficialPlugin(plugin1)
        manager.listPlugins().shouldContainExactlyInAnyOrder(plugin2)

        verify(exactly = 1) { s3Manager.downloadPluginCache("plugin-cache") }
        verify(exactly = 1) { s3Manager.uploadPluginCache("plugin-cache", pluginCacheFile) }
    }
}
