package edu.wpi.axon.core.dsl

/**
 * Matches valid Python identifiers.
 *
 * @param identifier The identifier to validate.
 * @return `true` if the identifier is valid.
 */
fun isValidPythonIdentifier(identifier: String): Boolean = when (identifier) {
    "_" -> false
    else -> identifier.contains(Regex("^[^\\d\\W]\\w*\\Z"))
}
