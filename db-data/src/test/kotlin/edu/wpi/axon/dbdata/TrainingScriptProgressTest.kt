package edu.wpi.axon.dbdata

import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class TrainingScriptProgressTest {

    @ParameterizedTest
    @MethodSource("testSerializationSource")
    fun `test serialize`(progress: TrainingScriptProgress) {
        TrainingScriptProgress.deserialize(progress.serialize()).shouldBe(progress)
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun testSerializationSource() = listOf(
            TrainingScriptProgress.NotStarted,
            TrainingScriptProgress.Creating,
            TrainingScriptProgress.Initializing,
            TrainingScriptProgress.InProgress(0.5),
            TrainingScriptProgress.Completed,
            TrainingScriptProgress.Error
        )
    }
}
