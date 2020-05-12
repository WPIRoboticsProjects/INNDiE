package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class LoadExampleDatasetTaskConfigurationTest :
    TaskConfigurationTestFixture<LoadExampleDatasetTask>(
        LoadExampleDatasetTask::class,
        listOf(
            LoadExampleDatasetTask::xTrainOutput,
            LoadExampleDatasetTask::yTrainOutput,
            LoadExampleDatasetTask::xTestOutput,
            LoadExampleDatasetTask::yTestOutput
        )
    )
