package edu.wpi.axon.dsl

import arrow.data.Valid
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
