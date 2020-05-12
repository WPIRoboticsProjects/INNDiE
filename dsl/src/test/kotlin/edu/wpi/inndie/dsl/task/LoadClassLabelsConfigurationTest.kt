package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class LoadClassLabelsConfigurationTest : TaskConfigurationTestFixture<LoadClassLabels>(
    { LoadClassLabels("").apply { classLabelsPath = "" } },
    listOf(
        LoadClassLabels::classOutput
    )
)
