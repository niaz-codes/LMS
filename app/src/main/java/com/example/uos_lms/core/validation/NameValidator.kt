package com.example.uos_lms.core.validation

object NameValidator {
    private val NAME_REGEX = Regex("^[A-Za-z ]{2,60}$")

    fun validate(name: String, fieldLabel: String): ValidationResult {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("$fieldLabel is required")
            !NAME_REGEX.matches(trimmed) -> ValidationResult.Invalid("$fieldLabel must contain only letters and spaces")
            else -> ValidationResult.Valid
        }
    }
}
