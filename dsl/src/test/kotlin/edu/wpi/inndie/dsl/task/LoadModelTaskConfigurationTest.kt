package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class LoadModelTaskConfigurationTest : TaskConfigurationTestFixture<LoadModelTask>(
    { LoadModelTask("").apply { modelPath = "" } },
    listOf(
        LoadModelTask::modelOutput
    )
)
