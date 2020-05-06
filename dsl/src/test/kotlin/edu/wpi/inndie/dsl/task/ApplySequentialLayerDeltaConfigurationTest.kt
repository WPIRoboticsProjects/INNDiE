package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class ApplySequentialLayerDeltaConfigurationTest :
    TaskConfigurationTestFixture<ApplySequentialLayerDeltaTask>(
        ApplySequentialLayerDeltaTask::class,
        listOf(
            ApplySequentialLayerDeltaTask::modelInput, ApplySequentialLayerDeltaTask::newModelOutput
        )
    )
