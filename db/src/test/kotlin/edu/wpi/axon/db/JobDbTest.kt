package edu.wpi.axon.db

import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.dbdata.nextDataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.nulls.shouldBeNull
import io.kotlintest.matchers.nulls.shouldNotBeNull
import io.kotlintest.shouldBe
import org.apache.commons.lang3.RandomStringUtils
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths
import kotlin.random.Random

internal class JobDbTest {
    @Test
    fun `create test`(@TempDir tempDir: File) {
        val db = createDb(tempDir)
        val job = Random.nextJob()

        val id = db.create(job)
        id.shouldNotBeNull()

        transaction {
            Jobs.select { Jobs.name eq job.name }
                .map { Jobs.toDomain(it) }
                .shouldContainExactly(job.copy(id = id))
        }
    }

    @Test
    fun `find by name test`(@TempDir tempDir: File) {
        val db = createDb(tempDir)
        val job = Random.nextJob()

        val id = db.create(job)!!

        db.findByName(job.name).shouldBe(job.copy(id = id))
    }

    @Test
    fun `count test`(@TempDir tempDir: File) {
        val db = createDb(tempDir)

        db.count().shouldBe(0)

        db.create(Random.nextJob())

        db.count().shouldBe(1)
    }

    @Test
    fun `remove test`(@TempDir tempDir: File) {
        val db = createDb(tempDir)
        val job = Random.nextJob()

        val id = db.create(job)!!

        db.remove(id).shouldBe(id)
        db.findByName(job.name).shouldBeNull()
    }


    private fun createDb(tempDir: File) = JobDb(
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
        nextBoolean(),
        -1
    )
}