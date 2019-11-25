package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture
import edu.wpi.axon.tfdata.Dataset

internal class LoadSuperviselyDatasetConfigurationTest :
    TaskConfigurationTestFixture<LoadSuperviselyDataset>(
        {
            LoadSuperviselyDataset("").apply {
                dataset = Dataset.Custom("a.tar", "a")
                bucketName = ""
            }
        },
        listOf(
            LoadSuperviselyDataset::xOutput,
            LoadSuperviselyDataset::yOutput
        )
    )
