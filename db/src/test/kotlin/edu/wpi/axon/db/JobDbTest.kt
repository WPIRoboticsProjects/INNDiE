package edu.wpi.axon.db

import edu.wpi.axon.dbdata.nextJob
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.nulls.shouldBeNull
import io.kotlintest.matchers.nulls.shouldNotBeNull
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
}
