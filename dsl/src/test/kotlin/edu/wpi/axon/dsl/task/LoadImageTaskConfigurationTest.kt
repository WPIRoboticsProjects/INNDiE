package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class LoadImageTaskConfigurationTest : TaskConfigurationTestFixture<LoadImageTask>(
    { LoadImageTask("").apply { imagePath = "" } },
    listOf(
        LoadImageTask::imageOutput
    )
)
