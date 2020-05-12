package edu.wpi.inndie.dsl.imports

import io.kotlintest.assertions.arrow.validation.shouldBeInvalid
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
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

        validator.validateImports(imports).shouldBeValid {
            it.a shouldBe imports
        }
    }

    @Test
    fun `spaces are not allowed in import names`() {
        val imports = setOf(Import.ModuleOnly("import 1"))
        validator.validateImports(imports).shouldBeInvalid()
    }
}
