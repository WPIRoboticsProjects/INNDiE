package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class RunInferenceTaskConfigurationTest : TaskConfigurationTestFixture<RunInferenceTask>(
    {
        RunInferenceTask("")
    },
    listOf(
        RunInferenceTask::model,
        RunInferenceTask::input,
        RunInferenceTask::steps,
        RunInferenceTask::output
    )
)
