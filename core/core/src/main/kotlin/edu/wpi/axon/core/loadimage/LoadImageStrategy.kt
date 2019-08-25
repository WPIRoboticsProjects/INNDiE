package edu.wpi.axon.core.loadimage

interface LoadImageStrategy {
    fun getPythonSegment(imageVariableName: String): String
}
