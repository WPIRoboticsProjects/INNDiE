package edu.wpi.axon.dsl

import arrow.core.Valid
import edu.wpi.axon.dsl.imports.ImportValidator
import edu.wpi.axon.dsl.validator.path.PathValidator
import io.mockk.every
import io.mockk.mockk
import org.koin.core.module.Module

fun Module.alwaysValidImportValidator() =
    single<ImportValidator> {
        mockk { every { validateImports(any()) } returns Valid(emptySet()) }
    }

fun Module.alwaysValidPathValidator() =
    single<PathValidator> {
        mockk { every { isValidPathName(any()) } returns true }
    }

fun Module.alwaysInvalidPathValidator() =
    single<PathValidator> {
        mockk { every { isValidPathName(any()) } returns false }
    }

fun mockUniqueVariableNameGenerator(): UniqueVariableNameGenerator =
    object : UniqueVariableNameGenerator {
        private var count = 1
        override fun uniqueVariableName() = synchronized(this) {
            "var${count++}"
        }
    }

fun Module.defaultUniqueVariableNameGenerator() = single { mockUniqueVariableNameGenerator() }
