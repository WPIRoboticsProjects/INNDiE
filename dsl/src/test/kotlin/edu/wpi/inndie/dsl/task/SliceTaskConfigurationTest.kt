package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class SliceTaskConfigurationTest : TaskConfigurationTestFixture<SliceTask>(
    {
        SliceTask("").apply {
            sliceNotation = ""
        }
    },
    listOf(SliceTask::input, SliceTask::output)
)
