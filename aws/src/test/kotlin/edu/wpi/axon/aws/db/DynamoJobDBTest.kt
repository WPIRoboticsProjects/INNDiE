package edu.wpi.axon.aws.db

import arrow.fx.IO
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.dbdata.nextDataset
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.shouldBe
import kotlin.random.Random
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.get
import org.koin.dsl.module
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

internal class DynamoJobDBTest : KoinTestFixture() {

    @Test
    @Disabled("Needs DynamoDB supervision.")
    fun `create new job`() {
        startKoin {
            modules(module {
                single { Region.US_EAST_1 }
            })
        }

        withRandomTable { db, tableName ->
            IO {
                val job = Random.nextJob()

                val result = db.createNewJob(job).attempt().unsafeRunSync()

                result.shouldBeRight {
                    val dbClient = DynamoDbClient.builder().region(get()).build()
                    dbClient.listTables().tableNames().shouldContain(tableName)
                    dbClient.getItem {
                        it.tableName(tableName)
                            .key(
                                mapOf(
                                    DynamoJobDB.KEY_JOB_NAME to AttributeValue.builder()
                                        .s(job.name)
                                        .build()
                                )
                            )
                    }.item().let { jobFromDB ->
                        jobFromDB[DynamoJobDB.KEY_JOB_NAME]!!.s().shouldBe(job.name)
                        jobFromDB[DynamoJobDB.KEY_DATA]!!.s().let { jobData ->
                            jobData.shouldBe(job.serialize())
                            Job.deserialize(jobData).shouldBe(job)
                        }
                    }
                }
            }
        }
    }

    @Test
    @Disabled("Needs DynamoDB supervision.")
    fun `update job status`() {
        startKoin {
            modules(module {
                single { Region.US_EAST_1 }
            })
        }

        withRandomTable { db, _ ->
            IO {
                val job = Random.nextJob()

                val result = db.createNewJob(job).attempt().unsafeRunSync()

                result.shouldBeRight {
                    db.getJobWithName(job.name).map {
                        it.shouldBe(job)
                    }.flatMap {
                        db.updateJobStatus(job, TrainingScriptProgress.NotStarted)
                    }.map {
                        it.shouldBe(job.copy(status = TrainingScriptProgress.NotStarted))
                    }.unsafeRunSync()
                }
            }
        }
    }

    /**
     * Runs the [testBody] with a [DynamoJobDB] and a random, newly created table. Deletes the table
     * when finished.
     *
     * @param testBody The body of the test method. Given a new [DynamoJobDB] and a table name.
     */
    private fun withRandomTable(testBody: (DynamoJobDB, String) -> IO<Unit>) {
        val tableName = RandomStringUtils.randomAlphanumeric(10)
        IO {
            DynamoJobDB(tableName)
        }.bracket(
            release = {
                it.deleteTable()
            },
            use = { testBody(it, tableName) }
        ).unsafeRunSync()
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
