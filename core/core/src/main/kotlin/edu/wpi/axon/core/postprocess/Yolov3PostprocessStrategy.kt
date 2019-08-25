package edu.wpi.axon.core.postprocess

class Yolov3PostprocessStrategy(
    private val boxesVariableName: String,
    private val scoresVariableName: String,
    private val indicesVariableName: String
) : PostprocessStrategy {

    override fun getPythonSegment() =
        """postprocessYolov3($boxesVariableName, $scoresVariableName, $indicesVariableName)"""
}
