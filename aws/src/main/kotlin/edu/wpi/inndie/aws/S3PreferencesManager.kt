package edu.wpi.inndie.aws

import edu.wpi.inndie.aws.preferences.Preferences
import edu.wpi.inndie.aws.preferences.PreferencesManager
import edu.wpi.inndie.util.singleAssign
import java.io.File
import java.nio.file.Files
import kotlin.concurrent.thread
import kotlin.properties.Delegates
import software.amazon.awssdk.core.exception.SdkClientException

/**
 * Manages the preferences data lifecycle. Stores preferences in S3.
 *
 * @param s3Manager Used for interfacing with S3.
 */
class S3PreferencesManager(
    private val s3Manager: S3Manager
) : PreferencesManager {

    private var preferencesFile by singleAssign<File>()
    private var workingPreferences by Delegates.notNull<Preferences>()

    override fun initialize() {
        // Download the preferences file or create a new empty one
        try {
            preferencesFile = s3Manager.downloadPreferences()
            workingPreferences = Preferences.deserialize(preferencesFile.readText())
        } catch (e: SdkClientException) {
            preferencesFile = Files.createTempFile("", "").toFile()
            workingPreferences = Preferences()
        }
    }

    override fun put(preferences: Preferences) {
        workingPreferences = preferences
        thread(isDaemon = true) {
            preferencesFile.writeText(workingPreferences.serialize())
            s3Manager.uploadPreferences(preferencesFile)
        }
    }

    override fun get(): Preferences = workingPreferences
}
