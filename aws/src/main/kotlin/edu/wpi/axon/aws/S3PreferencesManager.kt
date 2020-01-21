package edu.wpi.axon.aws

import Preferences
import PreferencesManager
import edu.wpi.axon.util.singleAssign
import java.io.File
import java.nio.file.Files
import kotlin.concurrent.thread
import kotlin.properties.Delegates
import software.amazon.awssdk.core.exception.SdkClientException

/**
 * Manages the preferences data lifecycle. Stores preferences in S3.
 *
 * @param bucketName The S3 bucket name to use for all S3 API calls.
 */
class S3PreferencesManager(bucketName: String) : PreferencesManager {

    private val s3Manager = S3Manager(bucketName)
    private var preferencesFile by singleAssign<File>()
    private var workingPreferences by Delegates.notNull<Preferences>()

    /**
     * This must be called before using [put] or [get]. This method initializes the local files
     * needed for preferences.
     */
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

    /**
     * Update the preferences.
     *
     * @param preferences The new preferences.
     */
    override fun put(preferences: Preferences) {
        workingPreferences = preferences
        thread(isDaemon = true) {
            preferencesFile.writeText(workingPreferences.serialize())
            s3Manager.uploadPreferences(preferencesFile)
        }
    }

    /**
     * Get the most recent preferences.
     *
     * @return The most recent preferences.
     */
    override fun get(): Preferences = workingPreferences
}
