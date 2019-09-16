package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class LoadClassLabelsConfigurationTest : TaskConfigurationTestFixture<LoadClassLabels>(
    { LoadClassLabels("").apply { classLabelsPath = "" } },
    listOf(
        LoadClassLabels::classOutput
    )
)
