package edu.wpi.axon.core.dsl

interface PathValidator {

    fun isValidPathName(pathName: String): Boolean
}
