package edu.wpi.axon.core.dsl.validator.path

interface PathValidator {

    fun isValidPathName(pathName: String): Boolean
}
