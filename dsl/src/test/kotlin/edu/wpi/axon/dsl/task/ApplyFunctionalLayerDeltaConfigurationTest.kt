package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class ApplyFunctionalLayerDeltaConfigurationTest :
    TaskConfigurationTestFixture<ApplyFunctionalLayerDeltaTask>(
        ApplyFunctionalLayerDeltaTask::class,
        listOf(
            ApplyFunctionalLayerDeltaTask::modelInput, ApplyFunctionalLayerDeltaTask::newModelOutput
        )
    )
