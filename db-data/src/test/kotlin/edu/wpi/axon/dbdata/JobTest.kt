package edu.wpi.axon.dbdata

import io.kotlintest.shouldBe
import kotlin.random.Random
import org.junit.jupiter.api.Test

internal class JobTest {

    @Test
    fun `test serialization`() {
        val jobBefore = Random.nextJob()
        Job.deserialize(jobBefore.serialize()).shouldBe(jobBefore)
    }
}
