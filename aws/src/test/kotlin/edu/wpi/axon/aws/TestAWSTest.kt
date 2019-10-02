package edu.wpi.axon.aws

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class TestAWSTest {

    @Test
    @Disabled("needs supervision to run (can start EC2 instances without stopping them)")
    fun `test uploadAndStartScript`() {
        val aws = TestAWS()
        val result =
            aws.uploadAndStartScript("""print("Hello, world!")""").attempt().unsafeRunSync()
        result.mapLeft { it.printStackTrace() }
    }
}
