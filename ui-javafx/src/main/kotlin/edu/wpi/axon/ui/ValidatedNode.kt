package edu.wpi.axon.ui

import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

sealed class ValidationResult {

    object Success : ValidationResult()

    data class Error(val message: String) : ValidationResult()

    operator fun times(other: ValidationResult): ValidationResult = when (this) {
        Success -> other
        is Error -> this
    }
}

class ValidatedNode<T : Node>(
    private val node: T,
    private val validate: ((T) -> ValidationResult),
    private val dependencies: Set<ValidatedNode<out Node>> = emptySet()
) : Region() {

    private var errorMessage: Label? = null
    private val container = VBox().apply {
        spacing = 5.0
        children.add(node)
    }

    init {
        children.add(container)

        when (node) {
            is ComboBox<*> -> {
                node.selectionModel.selectedItemProperty().addListener { _, _, _ ->
                    performValidation()
                }
            }
        }

        performValidation()
    }

    private fun performValidation(): ValidationResult {
        val result = validate(node) * dependencies.map { it.performValidation() }
            .fold<ValidationResult, ValidationResult>(
                ValidationResult.Success
            ) { acc, elem -> acc * elem }

        errorMessage?.let { container.children.remove(it) }
        errorMessage = null
        node.style = ""

        if (result is ValidationResult.Error) {
            errorMessage = Label(result.message).also {
                it.style = "-fx-text-fill: -validation-color-error-text;"
                container.children.add(it)
            }

            node.style = "-fx-text-fill: -validation-color-error-text;" +
                "-fx-border-color: -validation-color-error-border;"
        }

        return result
    }
}
