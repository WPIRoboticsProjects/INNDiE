package edu.wpi.axon.dsl

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

internal class DefaultUniqueVariableNameGeneratorTest {

    private val generator = DefaultUniqueVariableNameGenerator()

    @Test
    fun `test unique variable name generator on one thread`() {
        generator.uniqueVariableName() shouldBe "var1"
        generator.uniqueVariableName() shouldBe "var2"
        generator.uniqueVariableName() shouldBe "var3"
    }

    @Test
    fun `test unique variable name generator on two threads`() {
        val startLatch = CountDownLatch(1)
        val endLatch = CountDownLatch(2)

        thread {
            startLatch.await()
            repeat(10000) { generator.uniqueVariableName() }
            endLatch.countDown()
        }

        thread {
            startLatch.await()
            repeat(10000) { generator.uniqueVariableName() }
            endLatch.countDown()
        }

        startLatch.countDown()
        endLatch.await()
        generator.uniqueVariableName() shouldBe "var20001"
    }
}
