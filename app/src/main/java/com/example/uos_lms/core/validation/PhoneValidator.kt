package com.example.uos_lms.core.validation

object PhoneValidator {
    private val PATTERN = Regex("^03[0-9]{9}$")

    fun normalize(raw: String): String {
        var digits = raw.filter { it.isDigit() }
        if (digits.startsWith("92")) digits = "0" + digits.removePrefix("92")
        return digits
    }

    fun validate(raw: String): ValidationResult {
        val normalized = normalize(raw)
        return when {
            normalized.isEmpty() -> ValidationResult.Invalid("Phone number is required")
            !PATTERN.matches(normalized) -> ValidationResult.Invalid("Enter a valid phone number, e.g. 03001234567")
            else -> ValidationResult.Valid
        }
    }
}
