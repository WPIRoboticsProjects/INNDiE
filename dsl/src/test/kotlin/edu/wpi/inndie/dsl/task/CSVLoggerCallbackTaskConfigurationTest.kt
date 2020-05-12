package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

class CSVLoggerCallbackTaskConfigurationTest : TaskConfigurationTestFixture<CSVLoggerCallbackTask>(
    CSVLoggerCallbackTask::class,
    listOf(
        CSVLoggerCallbackTask::output
    )
)
