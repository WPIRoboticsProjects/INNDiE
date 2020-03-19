package edu.wpi.axon.db.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * The states a training script can be in.
 */
@Serializable
sealed class TrainingScriptProgress : Comparable<TrainingScriptProgress> {

    /**
     * The script has not been started yet.
     */
    @Serializable
    object NotStarted : TrainingScriptProgress()

    /**
     * The machine that is going to run the training script is being provisioned.
     */
    @Serializable
    object Creating : TrainingScriptProgress()

    /**
     * The machine that is going to run the training script is initializing the environment.
     */
    @Serializable
    object Initializing : TrainingScriptProgress()

    /**
     * The training is in progress.
     *
     * @param percentComplete The percent of epochs that have been completed.
     * @param progressLog The log of progress updates.
     */
    @Serializable
    data class InProgress(
        val percentComplete: Double,
        val progressLog: String
    ) : TrainingScriptProgress()

    /**
     * The training is finished.
     */
    @Serializable
    object Completed : TrainingScriptProgress()

    /**
     * The training script encountered an error.
     *
     * @param log Any error logs.
     */
    @Serializable
    data class Error(val log: String) : TrainingScriptProgress()

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    override fun compareTo(other: TrainingScriptProgress) = COMPARATOR.compare(this, other)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)

        private val COMPARATOR = Comparator.comparing<TrainingScriptProgress, Int> { it.ordinal() }
    }
}

inline fun <reified T : Any> T.ordinal() =
    T::class.java.superclass.classes.indexOfFirst { sub -> sub == this@ordinal::class.java }
