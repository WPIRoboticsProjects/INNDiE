package edu.wpi.axon.tasks.yolov3

import edu.wpi.axon.dsl.TaskConfigurationTestFixture

internal class LoadYoloV3ImageDataConfigurationTest : TaskConfigurationTestFixture<LoadYoloV3ImageData>(
    LoadYoloV3ImageData::class,
    listOf(
        LoadYoloV3ImageData::imageInput,
        LoadYoloV3ImageData::imageDataOutput,
        LoadYoloV3ImageData::imageSizeOutput
    )
)
