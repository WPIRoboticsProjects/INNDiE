package edu.wpi.axon.db.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * The method used to train a Job. Used to resume progress reporting after Axon is restarted.
 */
@Serializable
sealed class InternalJobTrainingMethod {

    /**
     * Training on an EC2 instance.
     *
     * @param instanceId The ID of the EC2 instance the Job was being trained on.
     */
    @Serializable
    data class EC2(val instanceId: String) : InternalJobTrainingMethod()

    /**
     * Training on the local computer.
     */
    @Serializable
    object Local : InternalJobTrainingMethod()

    /**
     * The Job has not been trained yet. Jobs should start in this state.
     */
    @Serializable
    object Untrained : InternalJobTrainingMethod()

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
