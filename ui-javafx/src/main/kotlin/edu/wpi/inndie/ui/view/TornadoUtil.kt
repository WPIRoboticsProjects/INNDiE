package edu.wpi.inndie.ui.view

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

fun String?.isDoubleLessThanOrEqualToZero() =
    if (this.isNullOrBlank()) {
        ValidationMessage(
            "Must not be empty.",
            ValidationSeverity.Error
        )
    } else {
        if (this.toDouble() <= 0.0) {
            null
        } else {
            ValidationMessage(
                "Must be less than or equal to zero.",
                ValidationSeverity.Error
            )
        }
    }

fun String?.isDoubleGreaterThanOrEqualToZero() =
    if (this.isNullOrBlank()) {
        ValidationMessage(
            "Must not be empty.",
            ValidationSeverity.Error
        )
    } else {
        if (this.toDouble() >= 0.0) {
            null
        } else {
            ValidationMessage(
                "Must be greater than or equal to zero.",
                ValidationSeverity.Error
            )
        }
    }

fun String?.isIntGreaterThanOrEqualTo(value: Int) =
    if (this.isNullOrBlank()) {
        ValidationMessage(
            "Must not be empty.",
            ValidationSeverity.Error
        )
    } else {
        if (this.replace(",", "").toInt() >= value) {
            null
        } else {
            ValidationMessage(
                "Must be greater than or equal to $value.",
                ValidationSeverity.Error
            )
        }
    }

fun String?.isDoubleInRange(range: ClosedRange<Double>) =
    if (this.isNullOrBlank()) {
        ValidationMessage("Must not be empty.", ValidationSeverity.Error)
    } else {
        if (this.toDouble() in range) {
            null
        } else {
            ValidationMessage("Must be in the range $range.", ValidationSeverity.Error)
        }
    }

fun String?.isLongInRange(range: ClosedRange<Long>) =
    if (this.isNullOrBlank()) {
        ValidationMessage("Must not be empty.", ValidationSeverity.Error)
    } else {
        if (this.replace(",", "").toLong() in range) {
            null
        } else {
            ValidationMessage("Must be in the range $range.", ValidationSeverity.Error)
        }
    }
