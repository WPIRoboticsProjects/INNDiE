package edu.wpi.axon.core.output

class Yolov3OutputStrategy(
    private val boxesVariableName: String,
    private val scoresVariableName: String,
    private val indicesVariableName: String
) : OutputStrategy {

    override fun getPythonSegment() =
        """$boxesVariableName, $scoresVariableName, $indicesVariableName"""
}
