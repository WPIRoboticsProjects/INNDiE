package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class RunEdgeTpuCompilerTaskConfigurationTest :
    TaskConfigurationTestFixture<RunEdgeTpuCompilerTask>(
        {
            RunEdgeTpuCompilerTask("").apply {
                inputModelFilename = "input.tflite"
            }
        },
        listOf()
    )
