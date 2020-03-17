package edu.wpi.axon.util

import arrow.core.Tuple3
import arrow.fx.IO
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import mu.KotlinLogging

val localCacheDir: Path = Paths.get(
    System.getProperty("user.home"),
    ".wpilib",
    "Axon"
)

val localScriptRunnerCache: Path = localCacheDir.resolve("local-script-runner-cache")

val trainingLogCsvFilename: Path = Paths.get("trainingLog.csv")

infix fun <E> Iterable<E>.anyIn(other: Iterable<E>) = any { it in other }

infix fun <E> Iterable<E>.allIn(other: Iterable<E>) = all { it in other }

infix fun <E> Iterable<E>.notAllIn(other: Iterable<E>) = !all { it in other }

fun Path.toPrintableString(): String = toString().replace("\\", "\\\\")

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
 * @param parentDir The directory the progress file is in.
 * @return The path to the progress reporting file used when Axon is running locally.
 */
fun createLocalProgressFilepath(parentDir: Path) = parentDir.resolve(trainingLogCsvFilename)

fun allS3OrLocal(vararg data: FilePath) = when (data.first()) {
    is FilePath.S3 -> data.all { it is FilePath.S3 }
    is FilePath.Local -> data.all { it is FilePath.Local }
}

/**
 * @param inputModelName The filename (no path component) of the model being trained.
 * @return The filename of the trained model.
 */
fun getOutputModelName(inputModelName: String): String =
    "${inputModelName.substringBeforeLast('.')}-trained.${inputModelName.substringAfterLast('.')}"

fun getLocalTrainingScriptRunnerWorkingDir(jobId: Int) =
    localScriptRunnerCache.resolve(jobId.toString()).apply { toFile().mkdirs() }

fun getLatestEpochFromProgressCsv(progressCsv: String): Int {
    val lines = progressCsv.lines()
    val epochColIndex = lines.first().split(',').indexOf("epoch")
    if (epochColIndex == -1) {
        throw IllegalStateException("Did not find the epoch column.")
    }

    return lines.mapNotNull { if (it.isBlank()) null else it }
        .last()
        .split(',')[epochColIndex]
        .toDouble()
        .toInt() // toDouble then toInt because toInt is too picky
}

private val LOGGER = KotlinLogging.logger { }
