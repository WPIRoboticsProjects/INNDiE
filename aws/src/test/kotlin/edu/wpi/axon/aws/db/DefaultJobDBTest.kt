package edu.wpi.axon.aws.db

import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.dbdata.nextDataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import io.kotlintest.matchers.collections.shouldContainExactly
import org.apache.commons.lang3.RandomStringUtils
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths
import kotlin.random.Random

internal class DefaultJobDBTest {

    @Test
    fun `test putting job`(@TempDir tempDir: File) {
        val db = createDb(tempDir)
        val job = Random.nextJob()

        db.putJob(job)

        transaction {
            JobEntity.find { Jobs.name eq job.name }.map { it.toJob() }.shouldContainExactly(job)
        }
    }

    private fun createDb(tempDir: File) = DefaultJobDB(
        Database.connect(
            url = "jdbc:h2:file:${Paths.get(tempDir.absolutePath, "test.db")}",
            driver = "org.h2.Driver"
        )
    )

    private fun Random.nextJob() = Job(
        RandomStringUtils.randomAlphanumeric(10),
        TrainingScriptProgress.Completed,
        RandomStringUtils.randomAlphanumeric(10),
        RandomStringUtils.randomAlphanumeric(10),
        nextDataset(),
        Optimizer.Adam(
            nextDouble(),
            nextDouble(),
            nextDouble(),
            nextDouble(),
            nextBoolean()
        ),
        Loss.SparseCategoricalCrossentropy,
        setOf(
            RandomStringUtils.randomAlphanumeric(10),
            RandomStringUtils.randomAlphanumeric(10)
        ),
        nextInt(),
        nextBoolean()
    )
}
