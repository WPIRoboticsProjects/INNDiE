package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class CompileModelConfigurationTest : TaskConfigurationTestFixture<CompileModelTask>(
    CompileModelTask::class,
    listOf(
        CompileModelTask::modelInput
    )
)
