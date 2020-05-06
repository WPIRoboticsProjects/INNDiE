package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class EarlyStoppingTaskConfigurationTest : TaskConfigurationTestFixture<EarlyStoppingTask>(
    EarlyStoppingTask::class,
    listOf(EarlyStoppingTask::output)
)
