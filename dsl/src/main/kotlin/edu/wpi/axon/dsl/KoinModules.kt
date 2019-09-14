package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.imports.DefaultImportValidator
import edu.wpi.axon.dsl.imports.ImportValidator
import edu.wpi.axon.dsl.validator.path.DefaultPathValidator
import edu.wpi.axon.dsl.validator.path.PathValidator
import edu.wpi.axon.dsl.validator.variablename.PythonVariableNameValidator
import edu.wpi.axon.dsl.validator.variablename.VariableNameValidator
import edu.wpi.axon.tfdata.code.DefaultLayerToCode
import edu.wpi.axon.tfdata.code.DefaultLossToCode
import edu.wpi.axon.tfdata.code.DefaultOptimizerToCode
import edu.wpi.axon.tfdata.code.LayerToCode
import edu.wpi.axon.tfdata.code.LossToCode
import edu.wpi.axon.tfdata.code.OptimizerToCode
import org.koin.dsl.module

fun defaultModule() = module {
    single<VariableNameValidator> { PythonVariableNameValidator() }
    single<PathValidator> { DefaultPathValidator() }
    single<ImportValidator> { DefaultImportValidator() }
    single<UniqueVariableNameGenerator> { DefaultUniqueVariableNameGenerator() }
    single<LayerToCode> { DefaultLayerToCode() }
    single<OptimizerToCode> { DefaultOptimizerToCode() }
    single<LossToCode> { DefaultLossToCode() }
}
