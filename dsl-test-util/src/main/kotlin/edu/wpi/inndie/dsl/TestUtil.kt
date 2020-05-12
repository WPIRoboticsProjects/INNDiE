package edu.wpi.inndie.dsl

import arrow.core.Valid
import edu.wpi.inndie.dsl.imports.ImportValidator
import edu.wpi.inndie.dsl.validator.path.PathValidator
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

fun mockUniqueVariableNameGeneratorImpl(): UniqueVariableNameGenerator =
    object : UniqueVariableNameGenerator {
        private var count = 1
        override fun uniqueVariableName() = synchronized(this) {
            "var${count++}"
        }
    }

fun Module.mockVariableNameGenerator() = single { mockUniqueVariableNameGeneratorImpl() }
