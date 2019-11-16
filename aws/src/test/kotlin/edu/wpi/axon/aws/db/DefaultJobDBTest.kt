package edu.wpi.axon.aws.db

import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.dbdata.nextDataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldHaveSize
import org.apache.commons.lang3.RandomStringUtils
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths
import kotlin.random.Random

object Users : IntIdTable() {
    val name = varchar("name", 50).index()
    val city = reference("city", Cities)
    val age = integer("age")
}

object Cities : IntIdTable() {
    val name = varchar("name", 50)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var city by City referencedOn Users.city
    var age by Users.age
}

class City(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<City>(Cities)

    var name by Cities.name
    val users by User referrersOn Users.city
}

internal class DefaultJobDBTest {

    @Test
    fun `test putting job`(@TempDir tempDir: File) {
        val db = createDb(tempDir)
        val job = Random.nextJob()

        db.putJob(job).unsafeRunSync()

        transaction {
            JobEntity.find { Jobs.name eq job.name }.toList().map { it.toJob() }.shouldContainExactly(job)
        }
    }

    @Test
    fun `test creating job`() {
        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Cities, Users)

            val stPete = City.new {
                name = "St. Petersburg"
            }

            val munich = City.new {
                name = "Munich"
            }

            User.new {
                name = "a"
                city = stPete
                age = 5
            }

            User.new {
                name = "b"
                city = stPete
                age = 27
            }

            User.new {
                name = "c"
                city = munich
                age = 42
            }

            println("Cities: ${City.all().joinToString { it.name }}")
            println("Users in ${stPete.name}: ${stPete.users.joinToString { it.name }}")
            println("Adults: ${User.find { Users.age greaterEq 18 }.joinToString { it.name }}")
        }
    }

    private fun createDb(tempDir: File): DefaultJobDB {
        return DefaultJobDB(Database.connect(
                url = "jdbc:h2:file:${Paths.get(tempDir.absolutePath, "test.db")}",
                driver = "org.h2.Driver"
            )
        )
    }

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
