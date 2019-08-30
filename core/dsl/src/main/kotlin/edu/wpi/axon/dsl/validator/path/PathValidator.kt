package edu.wpi.axon.dsl.validator.path

/**
 * Validates file paths.
 */
interface PathValidator {

    /**
     * Validates a file path.
     *
     * @param pathName The file path.
     * @return True if the [pathName] is valid.
     */
    fun isValidPathName(pathName: String): Boolean
}
