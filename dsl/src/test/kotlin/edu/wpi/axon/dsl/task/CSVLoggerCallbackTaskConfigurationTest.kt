package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

class CSVLoggerCallbackTaskConfigurationTest : TaskConfigurationTestFixture<CSVLoggerCallbackTask>(
    CSVLoggerCallbackTask::class,
    listOf(
        CSVLoggerCallbackTask::output
    )
)
