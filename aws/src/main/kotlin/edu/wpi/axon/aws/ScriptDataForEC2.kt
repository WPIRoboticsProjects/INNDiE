package edu.wpi.axon.aws

import arrow.core.None
import arrow.core.Option

/**
 * @param oldModelName The name of the current model (that will be loaded).
 * @param newModelName The name of the new model (that will be trained and saved).
 * @param datasetPathInS3 The path to the dataset in S3, or [None] if the dataset does not need
 * to be downloaded first.
 * @param scriptContents The contents of the training script.
 */
data class ScriptDataForEC2(
    val oldModelName: String,
    val newModelName: String,
    val datasetPathInS3: Option<String>,
    val scriptContents: String
)
