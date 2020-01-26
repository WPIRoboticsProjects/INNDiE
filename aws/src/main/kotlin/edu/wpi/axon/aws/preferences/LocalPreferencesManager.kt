package edu.wpi.axon.aws.preferences

import java.io.File
import java.nio.file.Path
import kotlin.properties.Delegates
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonDecodingException
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

    override fun get() = try {
        Preferences.deserialize(file.readText())
    } catch (ex: JsonDecodingException) {
        LOGGER.warn(ex) { "Failed to read preferences. Creating a new default preferences." }
        file.writeText(Preferences().serialize())
        Preferences()
    } catch (ex: SerializationException) {
        LOGGER.warn(ex) { "Failed to read preferences. Creating a new default preferences." }
        file.writeText(Preferences().serialize())
        Preferences()
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
