package edu.wpi.axon.preferences

import java.io.File
import java.nio.file.Path
import kotlin.properties.Delegates
import mu.KotlinLogging

/**
 * Manages the preferences data lifecycle. Stores preferences in a local file.
 */
class LocalPreferencesManager(
    private val preferencesFilePath: Path
) : PreferencesManager {

    private var file by Delegates.notNull<File>()

    override fun initialize() {
        file = preferencesFilePath.toFile()
        if (file.exists()) {
            if (file.readText().isEmpty()) {
                file.writeText(Preferences().serialize())
                LOGGER.info { "Created a new preferences file at $preferencesFilePath" }
            }
        } else {
            check(file.createNewFile()) {
                "Failed to create a new preferences file at $preferencesFilePath"
            }
        }
    }

    override fun put(preferences: Preferences) = file.writeText(preferences.serialize())

    override fun get() = Preferences.deserialize(file.readText())

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
