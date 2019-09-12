package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.imports.DefaultImportValidator
import edu.wpi.axon.dsl.imports.ImportValidator
import edu.wpi.axon.dsl.validator.path.DefaultPathValidator
import edu.wpi.axon.dsl.validator.path.PathValidator
import edu.wpi.axon.dsl.validator.variablename.PythonVariableNameValidator
import edu.wpi.axon.dsl.validator.variablename.VariableNameValidator
import edu.wpi.axon.tflayer.python.DefaultLayerToPythonCode
import edu.wpi.axon.tflayer.python.LayerToCode
import org.koin.dsl.module

fun defaultModule() = module {
    single<VariableNameValidator> { PythonVariableNameValidator() }
    single<PathValidator> { DefaultPathValidator() }
    single<ImportValidator> { DefaultImportValidator() }
    single<UniqueVariableNameGenerator> { DefaultUniqueVariableNameGenerator() }
    single<LayerToCode> { DefaultLayerToPythonCode() }
}
