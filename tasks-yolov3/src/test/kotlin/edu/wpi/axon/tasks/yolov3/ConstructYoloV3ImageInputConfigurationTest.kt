package edu.wpi.axon.tasks.yolov3

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class ConstructYoloV3ImageInputConfigurationTest : TaskConfigurationTestFixture<ConstructYoloV3ImageInput>(
    ConstructYoloV3ImageInput::class,
    listOf(
        ConstructYoloV3ImageInput::imageDataInput,
        ConstructYoloV3ImageInput::imageSizeInput,
        ConstructYoloV3ImageInput::sessionInput,
        ConstructYoloV3ImageInput::output
    )
)
