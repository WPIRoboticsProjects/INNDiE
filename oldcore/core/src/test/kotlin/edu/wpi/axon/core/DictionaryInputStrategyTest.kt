package edu.wpi.axon.core

import edu.wpi.axon.core.input.DictionaryInputStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DictionaryInputStrategyTest {

    @Test
    fun `both inputs should be specified in terms of the session inputs`() {
        val strategy = DictionaryInputStrategy("imgDataName", "imgSizeName")

        assertEquals(
            """{sessionInputsName[0].name: imgDataName, sessionInputsName[1].name: imgSizeName}""",
            strategy.getPythonSegment("sessionInputsName")
        )
    }
}
