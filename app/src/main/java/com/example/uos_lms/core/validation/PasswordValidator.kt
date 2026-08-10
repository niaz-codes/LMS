package com.example.uos_lms.core.validation

object PasswordValidator {
    fun validate(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Invalid("Password is required")
            password.length < 8 -> ValidationResult.Invalid("Password must be at least 8 characters")
            password.none { it.isDigit() } -> ValidationResult.Invalid("Password must contain at least one digit")
            password.none { it.isLetter() } -> ValidationResult.Invalid("Password must contain at least one letter")
            else -> ValidationResult.Valid
        }
    }

    fun validateConfirmation(password: String, confirmation: String): ValidationResult {
        return when {
            confirmation.isEmpty() -> ValidationResult.Invalid("Please confirm your password")
            confirmation != password -> ValidationResult.Invalid("Passwords do not match")
            else -> ValidationResult.Valid
        }
    }
}
