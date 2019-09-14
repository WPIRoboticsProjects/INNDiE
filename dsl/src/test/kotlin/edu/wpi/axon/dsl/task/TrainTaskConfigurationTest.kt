package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class TrainTaskConfigurationTest : TaskConfigurationTestFixture<TrainTask>(
    TrainTask::class,
    listOf(
        TrainTask::modelInput,
        TrainTask::trainInputData,
        TrainTask::trainOutputData,
        TrainTask::validationInputData,
        TrainTask::validationOutputData
    )
)
