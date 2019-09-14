package edu.wpi.axon.tasks.yolov3

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class YoloV3PostprocessTaskConfigurationTest : TaskConfigurationTestFixture<YoloV3PostprocessTask>(
    YoloV3PostprocessTask::class,
    listOf(
        YoloV3PostprocessTask::input, YoloV3PostprocessTask::output
    )
)
