package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class EarlyStoppingTaskConfigurationTest : TaskConfigurationTestFixture<EarlyStoppingTask>(
    EarlyStoppingTask::class,
    listOf(EarlyStoppingTask::output)
)
