package edu.wpi.axon.dbdata

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class TrainingScriptProgressTest {
    @Test
    fun `test serialize`() {
        TrainingScriptProgress.deserialize(TrainingScriptProgress.Completed.serialize()).shouldBe(TrainingScriptProgress.Completed)
    }
}
