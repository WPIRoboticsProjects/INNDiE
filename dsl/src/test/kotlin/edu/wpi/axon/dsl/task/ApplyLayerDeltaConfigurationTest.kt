package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class ApplyLayerDeltaConfigurationTest : TaskConfigurationTestFixture<ApplyLayerDeltaTask>(
    ApplyLayerDeltaTask::class,
    listOf(
        ApplyLayerDeltaTask::modelInput, ApplyLayerDeltaTask::newModelOutput
    )
)
