package edu.wpi.inndie.dsl

import edu.wpi.inndie.dsl.imports.Import
import edu.wpi.inndie.dsl.imports.makeImport
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ImportTest {

    @ParameterizedTest
    @MethodSource("importSource")
    fun `test imports`(importString: String, expected: Import) {
        makeImport(importString) shouldBe expected
        makeImport(importString).code() shouldBe expected.code()
    }

    @Test
    fun `invalid import`() {
        shouldThrow<IllegalArgumentException> { makeImport("") }
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun importSource() = listOf(
            Arguments.of("import foo", Import.ModuleOnly("foo")),
            Arguments.of("import foo as f", Import.ModuleAndName("foo", "f")),
            Arguments.of("from foo import f", Import.ModuleAndIdentifier("foo", "f")),
            Arguments.of("from foo import f as g", Import.FullImport("foo", "f", "g"))
        )
    }
}
