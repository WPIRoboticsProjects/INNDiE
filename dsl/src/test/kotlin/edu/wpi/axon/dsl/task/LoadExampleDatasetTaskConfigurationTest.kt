package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class LoadExampleDatasetTaskConfigurationTest :
    TaskConfigurationTestFixture<LoadExampleDatasetTask>(
        LoadExampleDatasetTask::class,
        listOf(
            LoadExampleDatasetTask::xTrain,
            LoadExampleDatasetTask::yTrain,
            LoadExampleDatasetTask::xTest,
            LoadExampleDatasetTask::yTest
        )
    )
