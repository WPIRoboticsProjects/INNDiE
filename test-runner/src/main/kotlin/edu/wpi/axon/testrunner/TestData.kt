package edu.wpi.axon.testrunner

import edu.wpi.axon.tfdata.Dataset
import java.nio.file.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

@Serializable
sealed class TestData {

    @Serializable
    data class FromExampleDataset(val exampleDataset: Dataset) : TestData()

    @Serializable
    data class FromFile(val filePath: Path) : TestData()

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)
}
