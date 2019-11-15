package edu.wpi.axon.aws.db

import com.amazonaws.regions.Regions
import edu.wpi.axon.testutil.KoinTestFixture
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import software.amazon.awssdk.regions.Region

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

internal class RDSJobDBTest : KoinTestFixture() {

    @Test
    @Disabled("Needs RDS supervision.")
    fun `test rds`() {
        startKoin {
            modules(module {
                single { Regions.US_EAST_1 }
                single { Region.US_EAST_1 }
            })
        }

        val db = RDSJobDB()
        db.ensureConfiguration().unsafeRunSync()

        Database.connect(
            url = "jdbc:mysql://axon-cluster.cluster-cnohjc8tu8oj.us-east-1.rds.amazonaws.com:3306",
            driver = "com.mysql.jdbc.Driver",
            user = "axonuser",
            password = "axonpassword"
        )

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
}
