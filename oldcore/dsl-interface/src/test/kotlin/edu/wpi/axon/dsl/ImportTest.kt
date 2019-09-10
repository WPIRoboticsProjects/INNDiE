package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ImportTest {

    @Test
    fun `module only`() {
        assertEquals(Import.ModuleOnly("foo"), makeImport("import foo"))
    }

    @Test
    fun `module and name`() {
        assertEquals(Import.ModuleAndName("foo", "f"), makeImport("import foo as f"))
    }

    @Test
    fun `module and identifier`() {
        assertEquals(Import.ModuleAndIdentifier("foo", "f"), makeImport("from foo import f"))
    }

    @Test
    fun `full import`() {
        assertEquals(Import.FullImport("foo", "f", "g"), makeImport("from foo import f as g"))
    }
}
