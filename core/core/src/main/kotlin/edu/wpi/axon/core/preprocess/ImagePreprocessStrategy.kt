package edu.wpi.axon.core.preprocess

interface ImagePreprocessStrategy {
    fun getPythonSegment(): String
}
