package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class ApplyLayerDeltaConfigurationTest : TaskConfigurationTestFixture<ApplySequentialLayerDeltaTask>(
    ApplySequentialLayerDeltaTask::class,
    listOf(
        ApplySequentialLayerDeltaTask::modelInput, ApplySequentialLayerDeltaTask::newModelOutput
    )
)
