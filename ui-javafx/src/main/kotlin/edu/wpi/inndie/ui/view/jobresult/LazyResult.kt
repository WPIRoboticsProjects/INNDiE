package edu.wpi.inndie.ui.view.jobresult

import java.io.File

/**
 * A training or testing result that lazy-loads the file to avoid downloading things from S3 when
 * possible.
 *
 * @param filename The filename of the [file].
 * @param file The file on the local machine; may involve downloading from S3.
 */
data class LazyResult(
    val filename: String,
    val file: Lazy<File>
)
