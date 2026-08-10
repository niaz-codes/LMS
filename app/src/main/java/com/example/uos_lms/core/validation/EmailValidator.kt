package com.example.uos_lms.core.validation

object EmailValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validate(email: String): ValidationResult {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Email is required")
            !EMAIL_REGEX.matches(trimmed) -> ValidationResult.Invalid("Enter a valid email address")
            else -> ValidationResult.Valid
        }
    }
}
