package edu.wpi.axon.testrunner

import edu.wpi.axon.tfdata.Dataset
import java.nio.file.Path

sealed class TestData {

    data class FromDataset(val exampleDataset: Dataset) : TestData()

    data class FromFile(val filePath: Path) : TestData()
}
