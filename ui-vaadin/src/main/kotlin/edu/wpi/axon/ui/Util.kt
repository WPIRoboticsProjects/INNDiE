package edu.wpi.axon.ui

import com.vaadin.flow.data.binder.ValidationResult
import com.vaadin.flow.data.binder.ValueContext

fun <T : Any?> validateNotEmpty(): (T, ValueContext) -> ValidationResult = { value: T, _: ValueContext ->
        if (value == null) {
            ValidationResult.error("Must not be empty.")
        } else {
            ValidationResult.ok()
        }
    }
