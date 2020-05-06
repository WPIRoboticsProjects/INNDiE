package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class CheckpointCallbackTaskConfigurationTest :
    TaskConfigurationTestFixture<CheckpointCallbackTask>(
        { CheckpointCallbackTask("").apply { filePath = "" } },
        listOf(
            CheckpointCallbackTask::output
        )
    )
