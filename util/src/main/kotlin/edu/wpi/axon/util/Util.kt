package edu.wpi.axon.util

import arrow.core.Tuple3
import arrow.fx.IO
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import mu.KotlinLogging

infix fun <E> Iterable<E>.anyIn(other: Iterable<E>) = any { it in other }

infix fun <E> Iterable<E>.allIn(other: Iterable<E>) = all { it in other }

infix fun <E> Iterable<E>.notAllIn(other: Iterable<E>) = !all { it in other }

/**
 * Runs a command as a process.
 *
 * @param command The command and its arguments.
 * @param env Any extra env vars.
 * @param dir The working directory of the process, or `null` to use the parent process' current directory.
 * @return The exit code, std out, and std err.
 */
@Suppress("BlockingMethodInNonBlockingContext")
fun runCommand(
    command: List<String>,
    env: Map<String, String>,
    dir: File?
): IO<Tuple3<Int, String, String>> = IO {
    LOGGER.debug { "Running command: ${command.joinToString { "\"$it\"" }}" }
    val proc = ProcessBuilder(command)
        .directory(dir)
        .also {
            val builderEnv = it.environment()
            env.forEach { (key, value) ->
                builderEnv[key] = value
            }
        }.start()

    BufferedReader(InputStreamReader(proc.inputStream)).useLines { procStdOut ->
        BufferedReader(InputStreamReader(proc.errorStream)).useLines { procStdErr ->
            val exitCode = proc.waitFor()
            Tuple3(
                exitCode,
                procStdOut.joinToString("\n"),
                procStdErr.joinToString("\n")
            )
        }
    }
}

/**
 * @param modelName The name of the model being trained.
 * @param datasetName The name of the dataset being trained on.
 * @return The path to the progress reporting file used when Axon is running locally.
 */
fun createProgressFilePath(modelName: String, datasetName: String) =
    "/tmp/progress_reporting/$modelName/$datasetName/progress.txt"

private val LOGGER = KotlinLogging.logger { }
