package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class PostTrainingQuantizationTaskConfigurationTest :
    TaskConfigurationTestFixture<PostTrainingQuantizationTask>(
        {
            PostTrainingQuantizationTask("").apply {
                modelFilename = "input.h5"
                outputModelFilename = "output.tflite"
            }
        },
        listOf(
            PostTrainingQuantizationTask::representativeDataset
        )
    )
