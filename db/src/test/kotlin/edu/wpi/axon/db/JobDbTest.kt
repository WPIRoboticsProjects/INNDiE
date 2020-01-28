package edu.wpi.axon.db

import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.dbdata.nextJob
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.nulls.shouldBeNull
import io.kotlintest.shouldBe
import java.io.File
import java.nio.file.Paths
import kotlin.random.Random
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class JobDbTest {

    @Test
    fun `create test`(@TempDir tempDir: File) {
        val db = createDb(tempDir)
        val job = Random.nextJob()

        val newJob = db.create(job)

        transaction {
            Jobs.select { Jobs.name eq job.name }
                .map { Jobs.toDomain(it) }
                .shouldContainExactly(newJob)
        }
    }

    @Test
    fun `update test`(@TempDir tempDir: File) {
        val db = createDb(tempDir)
        val job = db.create(Random.nextJob())

        job.name = "Test"
        db.update(job)

        transaction {
            Jobs.select { Jobs.id eq job.id }
                .map { Jobs.toDomain(it) }
                .shouldContainExactly(job)
        }
    }

    @Test
    fun `find by name test`(@TempDir tempDir: File) {
        val db = createDb(tempDir)
        val job = Random.nextJob()

        val newJob = db.create(job)

        db.findByName(job.name).shouldBe(newJob)
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

        val newJob = db.create(job)

        db.remove(newJob)
        db.findByName(job.name).shouldBeNull()
    }

    @Test
    fun `test fetching running jobs`(@TempDir tempDir: File) {
        val db = createDb(tempDir)

        val runningJobs = listOf(
            Random.nextJob().copy(status = TrainingScriptProgress.Creating),
            Random.nextJob().copy(status = TrainingScriptProgress.Initializing),
            Random.nextJob().copy(status = TrainingScriptProgress.InProgress(0.2))
        ).map { db.create(it) }

        // Jobs that are not running
        listOf(
            Random.nextJob().copy(status = TrainingScriptProgress.NotStarted),
            Random.nextJob().copy(status = TrainingScriptProgress.Completed),
            Random.nextJob().copy(status = TrainingScriptProgress.Error)
        ).map { db.create(it) }

        val runningJobsFromDb = db.fetchRunningJobs()
        runningJobsFromDb.shouldContainExactlyInAnyOrder(runningJobs)
    }

    private fun createDb(tempDir: File) = JobDb(
        Database.connect(
            url = "jdbc:h2:file:${Paths.get(tempDir.absolutePath, "test.db")}",
            driver = "org.h2.Driver"
        )
    )
}
