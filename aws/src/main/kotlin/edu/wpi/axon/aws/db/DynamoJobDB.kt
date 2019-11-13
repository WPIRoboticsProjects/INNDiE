package edu.wpi.axon.aws.db

import arrow.core.Left
import arrow.core.Right
import arrow.fx.IO
import kotlinx.coroutines.delay
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType
import software.amazon.awssdk.services.dynamodb.model.TableStatus

class DynamoJobDB(
    private val tableName: String,
    private val region: Region
) : JobDB {

    private val dbClient: IO<DynamoDbClient> = IO {
        DynamoDbClient.builder().region(region).build()
    }

    override fun createNewJob(job: Job): IO<Unit> = dbClient.flatMap { dbClient ->
        ensureJobTable(dbClient).flatMap {
            waitForTableStatus(dbClient, TableStatus.ACTIVE)
        }.flatMap {
            IO {
                dbClient.putItem {
                    it.tableName(tableName)
                        .item(
                            mapOf(
                                KEY_JOB_NAME to AttributeValue.builder().s(job.name).build(),
                                KEY_DATASET to AttributeValue.builder().s(job.dataset).build(),
                                KEY_DATA to AttributeValue.builder().n(job.data.toString()).build()
                            )
                        )
                }

                Unit
            }
        }
    }

    override fun deleteTable(): IO<Unit> = dbClient.flatMap { dbClient ->
        IO {
            dbClient.deleteTable {
                it.tableName(tableName)
            }

            Unit
        }
    }

    private fun ensureJobTable(client: DynamoDbClient): IO<Unit> {
        return IO {
            if (!client.listTables().tableNames().contains(tableName)) {
                // Only create the table if it does not exist
                client.createTable {
                    it.tableName(tableName)
                        .keySchema(
                            KeySchemaElement.builder()
                                .keyType(KeyType.HASH)
                                .attributeName(KEY_JOB_NAME)
                                .build(),
                            KeySchemaElement.builder()
                                .keyType(KeyType.RANGE)
                                .attributeName(KEY_DATASET)
                                .build()
                        )
                        .attributeDefinitions(
                            AttributeDefinition.builder()
                                .attributeName(KEY_JOB_NAME)
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                            AttributeDefinition.builder()
                                .attributeName(KEY_DATASET)
                                .attributeType(ScalarAttributeType.S)
                                .build()
                        )
                        .provisionedThroughput {
                            // TODO: Expose these
                            it.readCapacityUnits(1)
                                .writeCapacityUnits(1)
                        }
                }
            }

            Unit
        }
    }

    private fun waitForTableStatus(
        dbClient: DynamoDbClient,
        status: TableStatus
    ): IO<DynamoDbClient> =
        IO.tailRecM(dbClient) { i ->
            IO {
                if (i.describeTable { it.tableName(tableName) }.table().tableStatus() == status) {
                    Right(i)
                } else {
                    // Table is not at the status yet, so wait to check again
                    delay(500)
                    Left(i)
                }
            }
        }

    companion object {

        const val KEY_JOB_NAME = "job-name"
        const val KEY_DATASET = "dataset"
        const val KEY_DATA = "data"
    }
}
