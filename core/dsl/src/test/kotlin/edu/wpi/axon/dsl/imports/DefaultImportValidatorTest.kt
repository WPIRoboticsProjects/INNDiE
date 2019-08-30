package edu.wpi.axon.dsl.imports

import arrow.data.Valid
import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.testutil.isInvalid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DefaultImportValidatorTest {

    private val validator = DefaultImportValidator()

    @Test
    fun `a valid set of imports is passed through without modification`() {
        val imports = setOf(
            Import.ModuleOnly("import1"),
            Import.ModuleOnly("import2"),
            Import.ModuleAndIdentifier("import1", "id1")
        )

        assertEquals(validator.validateImports(imports), Valid(imports))
    }

    @Test
    fun `spaces are not allowed in import names`() {
        val imports = setOf(Import.ModuleOnly("import 1"))
        assertThat(validator.validateImports(imports), isInvalid())
    }
}
