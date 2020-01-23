package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class DownloadUntrainedModelFromS3TaskConfigurationTest :
    TaskConfigurationTestFixture<DownloadUntrainedModelFromS3Task>(
        { DownloadUntrainedModelFromS3Task("") },
        listOf()
    )

internal class DownloadUntrainedModelFromS3TaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin { }

        val task = DownloadUntrainedModelFromS3Task("").apply {
            modelName = "modelName.h5"
            bucketName = "bucketName"
        }

        task.code() shouldBe """
            |axon.client.impl_download_untrained_model("modelName.h5", "bucketName", None)
        """.trimMargin()
    }
}
