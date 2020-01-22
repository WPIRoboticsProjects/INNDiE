package edu.wpi.axon.aws.preferences

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import software.amazon.awssdk.services.ec2.model.InstanceType

/**
 * The user's edu.wpi.axon.aws.preferences.
 *
 * @param defaultEC2NodeType The default EC2 node type to run training scripts in.
 */
@Serializable
data class Preferences(
    val defaultEC2NodeType: InstanceType = InstanceType.T2_MICRO
) {

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
