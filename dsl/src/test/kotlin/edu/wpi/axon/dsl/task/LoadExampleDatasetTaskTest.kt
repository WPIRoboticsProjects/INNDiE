package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.code.DatasetToCode
import io.kotlintest.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class LoadExampleDatasetTaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        val mockDataset = mockk<Dataset.ExampleDataset> { every { name } returns "dataset_name" }
        val mockDatasetToCode = mockk<DatasetToCode> {
            every { datasetToCode(mockDataset) } returns "dataset_code"
        }

        startKoin {
            modules(module {
                single { mockDatasetToCode }
            })
        }

        val task = LoadExampleDatasetTask("task").apply {
            dataset = mockDataset
            xTrainOutput = configuredCorrectly("xTrain")
            yTrainOutput = configuredCorrectly("yTrain")
            xTestOutput = configuredCorrectly("xTest")
            yTestOutput = configuredCorrectly("yTest")
        }

        task.code() shouldBe """
            |(xTrain, yTrain), (xTest, yTest) = dataset_code.load_data()
        """.trimMargin()

        verify { mockDatasetToCode.datasetToCode(mockDataset) }
        confirmVerified(mockDatasetToCode)
    }
}
