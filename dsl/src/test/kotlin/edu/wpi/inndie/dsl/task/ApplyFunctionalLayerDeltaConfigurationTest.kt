package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture

internal class ApplyFunctionalLayerDeltaConfigurationTest :
    TaskConfigurationTestFixture<ApplyFunctionalLayerDeltaTask>(
        ApplyFunctionalLayerDeltaTask::class,
        listOf(
            ApplyFunctionalLayerDeltaTask::modelInput, ApplyFunctionalLayerDeltaTask::newModelOutput
        )
    )
