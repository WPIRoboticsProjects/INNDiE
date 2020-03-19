package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class LoadStringTaskConfigurationTest : TaskConfigurationTestFixture<LoadStringTask>(
    {
        LoadStringTask("").apply {
            data = ""
        }
    },
    listOf(LoadStringTask::output)
)
