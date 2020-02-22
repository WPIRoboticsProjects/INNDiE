package edu.wpi.axon.ui.view

import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.control.TextField
import tornadofx.ValidationMessage
import tornadofx.ValidationSeverity
import tornadofx.bind
import tornadofx.textfield

@JvmName("textfieldDouble")
fun EventTarget.textfield(property: ObservableValue<Double>, op: TextField.() -> Unit = {}) =
    textfield().apply {
        bind(property)
        op(this)
    }

fun isNotNull(value: String?) =
    if (value == null) {
        ValidationMessage(
            "Must not be null.",
            ValidationSeverity.Error
        )
    } else {
        null
    }

fun isDoubleLessThanOrEqualToZero(value: String?) =
    if (value == null) {
        ValidationMessage(
            "Must not be null.",
            ValidationSeverity.Error
        )
    } else {
        if (value.toDouble() <= 0.0) {
            null
        } else {
            ValidationMessage(
                "Must be less than or equal to zero.",
                ValidationSeverity.Error
            )
        }
    }

fun isDoubleGreaterThanOrEqualToZero(value: String?) =
    if (value == null) {
        ValidationMessage(
            "Must not be null.",
            ValidationSeverity.Error
        )
    } else {
        if (value.toDouble() >= 0.0) {
            null
        } else {
            ValidationMessage(
                "Must be greater than or equal to zero.",
                ValidationSeverity.Error
            )
        }
    }

fun isLongGreaterThanOrEqualToZero(value: String?) =
    if (value == null) {
        ValidationMessage(
            "Must not be null.",
            ValidationSeverity.Error
        )
    } else {
        if (value.replace(",", "").toLong() >= 0L) {
            null
        } else {
            ValidationMessage(
                "Must be greater than or equal to zero.",
                ValidationSeverity.Error
            )
        }
    }

fun isDoubleInRange(value: String?, range: ClosedRange<Double>) =
    if (value == null) {
        ValidationMessage("Must not be null.", ValidationSeverity.Error)
    } else {
        if (value.toDouble() in range) {
            null
        } else {
            ValidationMessage("Must be in the range $range.", ValidationSeverity.Error)
        }
    }

fun isLongInRange(value: String?, range: ClosedRange<Long>) =
    if (value.isNullOrBlank()) {
        ValidationMessage("Must not be null.", ValidationSeverity.Error)
    } else {
        if (value.replace(",", "").toLong() in range) {
            null
        } else {
            ValidationMessage("Must be in the range $range.", ValidationSeverity.Error)
        }
    }
