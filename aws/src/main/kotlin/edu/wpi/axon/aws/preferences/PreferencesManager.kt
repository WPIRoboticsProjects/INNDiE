package edu.wpi.axon.aws.preferences

/**
 * Manages the edu.wpi.axon.aws.preferences data lifecycle.
 */
interface PreferencesManager {

    /**
     * This must be called before using [put] or [get]. This method initializes the local files
     * needed for edu.wpi.axon.aws.preferences.
     */
    fun initialize()

    /**
     * Update the edu.wpi.axon.aws.preferences.
     *
     * @param preferences The new edu.wpi.axon.aws.preferences.
     */
    fun put(preferences: Preferences)

    /**
     * Get the most recent edu.wpi.axon.aws.preferences.
     *
     * @return The most recent edu.wpi.axon.aws.preferences.
     */
    fun get(): Preferences
}
