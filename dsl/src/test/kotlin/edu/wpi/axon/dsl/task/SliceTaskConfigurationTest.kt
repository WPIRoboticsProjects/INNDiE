package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class SliceTaskConfigurationTest : TaskConfigurationTestFixture<SliceTask>(
    {
        SliceTask("").apply {
            sliceNotation = ""
        }
    },
    listOf(SliceTask::input, SliceTask::output)
)
