package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class RunEdgeTpuCompilerTaskConfigurationTest :
    TaskConfigurationTestFixture<RunEdgeTpuCompilerTask>(
        {
            RunEdgeTpuCompilerTask("").apply {
                inputModelFilename = "input.tflite"
            }
        },
        listOf()
    )
