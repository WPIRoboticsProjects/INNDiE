package edu.wpi.axon.core.loadimage

class LoadImageFromDiskStrategy(
    private val imagePath: String
) : LoadImageStrategy {

    override fun getPythonSegment(imageVariableName: String): String =
        """$imageVariableName = Image.open('$imagePath')"""
}
