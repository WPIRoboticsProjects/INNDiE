package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.dsl.mockVariableNameGenerator
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.inndie.tfdata.Dataset
import edu.wpi.inndie.tfdata.code.ExampleDatasetToCode
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
        val mockDatasetToCode = mockk<ExampleDatasetToCode> {
            every { datasetToCode(mockDataset) } returns "    return tf.keras.datasets.my_dataset.load_data()"
        }

        startKoin {
            modules(module {
                single { mockDatasetToCode }
                mockVariableNameGenerator()
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
            |def var1():
            |    return tf.keras.datasets.my_dataset.load_data()
            |
            |(xTrain, yTrain), (xTest, yTest) = var1()
        """.trimMargin()

        verify { mockDatasetToCode.datasetToCode(mockDataset) }
        confirmVerified(mockDatasetToCode)
    }
}
