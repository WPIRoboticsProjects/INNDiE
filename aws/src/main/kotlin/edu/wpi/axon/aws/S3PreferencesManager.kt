package edu.wpi.axon.aws

import edu.wpi.axon.aws.preferences.Preferences
import edu.wpi.axon.aws.preferences.PreferencesManager
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
