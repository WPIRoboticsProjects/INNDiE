package edu.wpi.axon.aws.preferences

import io.kotlintest.shouldBe
import java.io.File
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import software.amazon.awssdk.services.ec2.model.InstanceType

internal class LocalPreferencesManagerTest {

    @Test
    fun `test initialize and get`(@TempDir tempDir: File) {
        val manager = LocalPreferencesManager(File(tempDir, "preferences.json").toPath())
        manager.initialize()
        manager.get().shouldBe(Preferences())
    }

    @Test
    fun `test put and get`(@TempDir tempDir: File) {
        val manager = LocalPreferencesManager(File(tempDir, "preferences.json").toPath())
        manager.initialize()
        val newPrefs = Preferences(defaultEC2NodeType = InstanceType.A1_2_XLARGE)
        manager.put(newPrefs)
        manager.get().shouldBe(newPrefs)
    }
}
