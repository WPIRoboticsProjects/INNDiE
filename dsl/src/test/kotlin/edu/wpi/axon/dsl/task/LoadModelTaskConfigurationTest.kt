package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class LoadModelTaskConfigurationTest : TaskConfigurationTestFixture<LoadModelTask>(
    { LoadModelTask("").apply { modelPath = "" } },
    listOf(
        LoadModelTask::modelOutput
    )
)
