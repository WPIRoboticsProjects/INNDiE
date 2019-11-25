package edu.wpi.axon.dsl.task

import arrow.core.None
import arrow.core.Some
import edu.wpi.axon.dsl.TaskConfigurationTestFixture
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class DownloadModelFromS3TaskConfigurationTest :
    TaskConfigurationTestFixture<DownloadModelFromS3Task>(
        { DownloadModelFromS3Task("") },
        listOf()
    )

internal class DownloadModelFromS3TaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin { }

        val task = DownloadModelFromS3Task("").apply {
            modelName = "modelName.h5"
            bucketName = "bucketName"
            region = Some("region-name")
        }

        task.code() shouldBe """
            |axon.client.impl_download_model_file("modelName.h5", "bucketName", "region-name")
        """.trimMargin()
    }

    @Test
    fun `test code gen without a specified region`() {
        startKoin { }

        val task = DownloadModelFromS3Task("").apply {
            modelName = "modelName.h5"
            bucketName = "bucketName"
            region = None
        }

        task.code() shouldBe """
            |axon.client.impl_download_model_file("modelName.h5", "bucketName", None)
        """.trimMargin()
    }
}
