package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

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
