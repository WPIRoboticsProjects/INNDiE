package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

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
