package edu.wpi.axon.aws.db

import arrow.fx.IO
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.shouldBe
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

internal class DynamoJobDBTest {

    private val region = Region.US_EAST_1

    @Test
    @Disabled("Needs supervision.")
    fun `create new job`() {
        withRandomTable { db, tableName ->
            IO {
                val job = Job(
                    RandomStringUtils.randomAlphanumeric(10),
                    RandomStringUtils.randomAlphanumeric(10),
                    42
                )

                val result = db.createNewJob(job).attempt().unsafeRunSync()

                result.shouldBeRight {
                    val dbClient = DynamoDbClient.builder().region(region).build()
                    dbClient.listTables().tableNames().shouldContain(tableName)
                    dbClient.getItem {
                        it.tableName(tableName)
                            .key(
                                mapOf(
                                    DynamoJobDB.KEY_JOB_NAME to AttributeValue.builder()
                                        .s(job.name)
                                        .build(),
                                    DynamoJobDB.KEY_DATASET to AttributeValue.builder()
                                        .s(job.dataset)
                                        .build()
                                )
                            )
                    }.item().let { jobFromDB ->
                        jobFromDB[DynamoJobDB.KEY_JOB_NAME]!!.s().shouldBe(job.name)
                        jobFromDB[DynamoJobDB.KEY_DATASET]!!.s().shouldBe(job.dataset)
                        jobFromDB[DynamoJobDB.KEY_DATA]!!.n().toInt().shouldBe(job.data)
                    }
                }
            }
        }
    }

    /**
     * Runs the [testBody] with a [DynamoJobDB] and a random, newly created table. Deleted the table
     * when finished.
     *
     * @param testBody The body of the test method. Given a new [DynamoJobDB] and a table name.
     */
    private fun withRandomTable(testBody: (DynamoJobDB, String) -> IO<Unit>) {
        val tableName = RandomStringUtils.randomAlphanumeric(10)
        IO {
            DynamoJobDB(tableName, region)
        }.bracket(
            release = {
                it.deleteTable()
            },
            use = { testBody(it, tableName) }
        ).unsafeRunSync()
    }
}