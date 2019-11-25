package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture
import edu.wpi.axon.tfdata.Dataset

internal class LoadTFRecordOfImagesWithObjectsConfigurationTest :
    TaskConfigurationTestFixture<LoadTFRecordOfImagesWithObjects>(
        {
            LoadTFRecordOfImagesWithObjects("").apply {
                dataset = Dataset.Custom("a.tar", "a")
                bucketName = ""
            }
        },
        listOf(
            LoadTFRecordOfImagesWithObjects::xOutput,
            LoadTFRecordOfImagesWithObjects::yOutput
        )
    )
