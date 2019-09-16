package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class ReshapeAndScaleTaskConfigurationTest :
    TaskConfigurationTestFixture<ReshapeAndScaleTask>(
        {
            ReshapeAndScaleTask("").apply {
                reshapeArgs = listOf()
                scale = null
            }
        },
        listOf(
            ReshapeAndScaleTask::input,
            ReshapeAndScaleTask::output
        )
    )
