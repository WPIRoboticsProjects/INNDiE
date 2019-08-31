package edu.wpi.axon.testutil

import org.junit.jupiter.api.AfterEach
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

open class KoinTestFixture : KoinTest {

    private var additionalAfterEach: () -> Unit = {}

    @AfterEach
    fun afterEach() {
        additionalAfterEach()
        stopKoin()
    }

    fun additionalAfterEach(configure: () -> Unit) {
        additionalAfterEach = configure
    }
}
