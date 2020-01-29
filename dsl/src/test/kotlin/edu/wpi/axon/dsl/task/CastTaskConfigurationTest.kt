package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class CastTaskConfigurationTest : TaskConfigurationTestFixture<CastTask>(
    {
        CastTask("").apply {
            dtype = ""
        }
    },
    listOf(CastTask::input, CastTask::output)
)
