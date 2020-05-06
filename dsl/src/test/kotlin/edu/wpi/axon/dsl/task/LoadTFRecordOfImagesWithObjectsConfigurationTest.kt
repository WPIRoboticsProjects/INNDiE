package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture
import edu.wpi.inndie.tfdata.Dataset
import edu.wpi.inndie.util.FilePath

internal class LoadTFRecordOfImagesWithObjectsConfigurationTest :
    TaskConfigurationTestFixture<LoadTFRecordOfImagesWithObjects>(
        {
            LoadTFRecordOfImagesWithObjects("").apply {
                dataset = Dataset.Custom(FilePath.Local("a.tar"), "a")
            }
        },
        listOf(
            LoadTFRecordOfImagesWithObjects::xOutput,
            LoadTFRecordOfImagesWithObjects::yOutput
        )
    )
