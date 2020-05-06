package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class LoadImageTaskConfigurationTest : TaskConfigurationTestFixture<LoadImageTask>(
    { LoadImageTask("").apply { imagePath = "" } },
    listOf(
        LoadImageTask::imageOutput
    )
)
