package edu.wpi.axon.dsl.task

import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.Test
import org.koin.core.context.startKoin

internal class EnableEagerExecutionTaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin { }
        EnableEagerExecutionTask("").code().shouldBe("tf.enable_eager_execution()")
    }
}
