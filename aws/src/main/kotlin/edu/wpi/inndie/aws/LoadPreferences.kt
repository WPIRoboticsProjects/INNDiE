package edu.wpi.inndie.aws

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import mu.KotlinLogging
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.s3.S3Client

/**
 * Finds the S3 bucket INNDiE will work out of. Returns [None] if there is no matching bucket, which
 * causes INNDiE to run locally and not interface with AWS. The AWS region MUST be auto-detectable
 * from the environment (like when running on ECS). To use AWS when running locally, set
 * `AWS_REGION` to your preferred region. To not use AWS when running locally, do not set
 * `AWS_REGION`.
 *
 * @return The name of the bucket or [None] if the bucket could not be found.
 */
fun findINNDiES3Bucket(): Option<String> = try {
    val s3Client = S3Client.builder().build()

    val bucket = s3Client.listBuckets().buckets().first {
        it.name().startsWith("axon-autogenerated-")
    }

    LOGGER.info { "Starting with S3 bucket: $bucket" }
    Some(bucket.name())
} catch (e: SdkClientException) {
    LOGGER.info(e) { "Not loading credentials because of this exception." }
    None
}

private val LOGGER = KotlinLogging.logger { }
