package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class LoadStringTaskConfigurationTest : TaskConfigurationTestFixture<LoadStringTask>(
    {
        LoadStringTask("").apply {
            data = ""
        }
    },
    listOf(LoadStringTask::output)
)
