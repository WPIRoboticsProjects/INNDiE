package edu.wpi.axon.aws.preferences

/**
 * Manages the preferences data lifecycle.
 */
interface PreferencesManager {

    /**
     * This must be called before using [put] or [get]. This method initializes the local files
     * needed for preferences.
     */
    fun initialize()

    /**
     * Update the preferences.
     *
     * @param preferences The new preferences.
     */
    fun put(preferences: Preferences)

    /**
     * Get the most recent preferences.
     *
     * @return The most recent preferences.
     */
    fun get(): Preferences
}
