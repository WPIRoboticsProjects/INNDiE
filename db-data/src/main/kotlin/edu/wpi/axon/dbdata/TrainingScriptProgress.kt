package edu.wpi.axon.dbdata

import edu.wpi.axon.util.ObjectSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule

/**
 * The states a training script can be in.
 */
sealed class TrainingScriptProgress : Comparable<TrainingScriptProgress> {

    /**
     * The script has not been started yet.
     */
    object NotStarted : TrainingScriptProgress()

    /**
     * The training is in progress.
     *
     * @param percentComplete The percent of epochs that have been completed.
     */
    @Serializable
    data class InProgress(val percentComplete: Double) : TrainingScriptProgress()

    /**
     * The training is finished.
     */
    object Completed : TrainingScriptProgress()

    fun serialize(): String = Json(
        JsonConfiguration.Stable,
        context = trainingScriptProgressModule
    ).stringify(PolymorphicWrapper.serializer(), PolymorphicWrapper(this))

    override fun compareTo(other: TrainingScriptProgress): Int {
        return COMPARATOR.compare(this, other)
    }

    companion object {
        fun deserialize(data: String): TrainingScriptProgress = Json(
            JsonConfiguration.Stable,
            context = trainingScriptProgressModule
        ).parse(PolymorphicWrapper.serializer(), data).wrapped

        private val COMPARATOR = Comparator.comparing<TrainingScriptProgress, Int> { it.ordinal() }
    }

    @Serializable
    private data class PolymorphicWrapper(@Polymorphic val wrapped: TrainingScriptProgress)
}

val trainingScriptProgressModule = SerializersModule {
    polymorphic<TrainingScriptProgress> {
        addSubclass(
            TrainingScriptProgress.NotStarted::class,
            ObjectSerializer(TrainingScriptProgress.NotStarted)
        )
        addSubclass(
            TrainingScriptProgress.InProgress::class,
            TrainingScriptProgress.InProgress.serializer()
        )
        addSubclass(
            TrainingScriptProgress.Completed::class,
            ObjectSerializer(TrainingScriptProgress.Completed)
        )
    }
}

inline fun <reified T : Any> T.ordinal() =
    T::class.java.superclass.classes.indexOfFirst { sub -> sub == this@ordinal::class.java }
