package edu.wpi.axon.core.dsl

import java.nio.file.InvalidPathException
import java.nio.file.Paths

class DefaultPathValidator : PathValidator {

    @SuppressWarnings("SwallowedException")
    override fun isValidPathName(pathName: String): Boolean {
        if (pathName.isBlank()) {
            return false
        }

        return try {
            val file = Paths.get(pathName).toFile()
            file.isFile
        } catch (ex: InvalidPathException) {
            false
        }
    }
}