package com.example.uos_lms.core.validation

object CnicValidator {
    fun normalize(raw: String): String = raw.filter { it.isDigit() }

    fun validate(raw: String): ValidationResult {
        val digits = normalize(raw)
        return when {
            digits.isEmpty() -> ValidationResult.Invalid("CNIC is required")
            digits.length != 13 -> ValidationResult.Invalid("CNIC must be 13 digits")
            else -> ValidationResult.Valid
        }
    }
}
