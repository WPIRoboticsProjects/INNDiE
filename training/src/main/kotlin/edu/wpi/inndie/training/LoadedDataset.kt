package edu.wpi.inndie.training

import arrow.core.None
import arrow.core.Option
import edu.wpi.inndie.dsl.variable.Variable

/**
 * Represents a dataset after it has been loaded into the script.
 *
 * @param train The x and y variables to train on.
 * @param validation The x and y variables to validate on, or [None] to not specify any validation
 * dataset.
 * @param validationSplit The validation split, or [None] to not specify a validation split.
 */
data class LoadedDataset(
    val train: Pair<Variable, Variable>,
    val validation: Option<Pair<Variable, Variable>>,
    val validationSplit: Option<Double>
)
