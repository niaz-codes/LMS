package com.example.uos_lms.core.validation

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult
}

val ValidationResult.isValid: Boolean get() = this is ValidationResult.Valid
val ValidationResult.errorMessageOrNull: String?
    get() = (this as? ValidationResult.Invalid)?.message
