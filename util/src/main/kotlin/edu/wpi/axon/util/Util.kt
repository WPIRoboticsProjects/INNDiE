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

    LOGGER.debug { "Started on PID ${proc.pid()}" }

    BufferedReader(InputStreamReader(proc.inputStream)).useLines { procStdOut ->
        BufferedReader(InputStreamReader(proc.errorStream)).useLines { procStdErr ->
            val exitCode = try {
                proc.waitFor()
            } catch (ex: InterruptedException) {
                // An interruption means that the caller wants the command to be stopped immediately
                proc.destroyForcibly()
                throw RuntimeException("Forcibly destroyed the runCommand process.", ex)
            }

            Tuple3(
                exitCode,
                procStdOut.joinToString("\n"),
                procStdErr.joinToString("\n")
            )
        }
    }
}

/**
 * @param progressReportingDirPrefix The prefix for the local progress reporting directory.
 * @param id The unique Job ID.
 * @return The path to the progress reporting file used when Axon is running locally.
 */
fun createLocalProgressFilepath(progressReportingDirPrefix: String, id: Int) =
    "$progressReportingDirPrefix/$id/progress.txt"

fun allS3OrLocal(vararg data: FilePath) = when (data.first()) {
    is FilePath.S3 -> data.all { it is FilePath.S3 }
    is FilePath.Local -> data.all { it is FilePath.Local }
}

fun getOutputModelName(inputModelName: String): String =
    "${inputModelName.substringBeforeLast('.')}-trained.${inputModelName.substringAfterLast('.')}"

private val LOGGER = KotlinLogging.logger { }
