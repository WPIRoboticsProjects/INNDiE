package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

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
