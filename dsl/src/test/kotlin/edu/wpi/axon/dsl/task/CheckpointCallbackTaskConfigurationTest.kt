package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class CheckpointCallbackTaskConfigurationTest :
    TaskConfigurationTestFixture<CheckpointCallbackTask>(
        { CheckpointCallbackTask("").apply { filePath = "" } },
        listOf(
            CheckpointCallbackTask::output
        )
    )
