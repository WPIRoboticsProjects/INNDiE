package edu.wpi.inndie.tfdata

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class VerbosityTest {

    @Test
    fun `Silent has value of 0`() {
        Verbosity.Silent.value shouldBe 0
    }

    @Test
    fun `ProgressBar has value of 1`() {
        Verbosity.ProgressBar.value shouldBe 1
    }

    @Test
    fun `OneLinePerEpoch has value of 2`() {
        Verbosity.OneLinePerEpoch.value shouldBe 2
    }
}
