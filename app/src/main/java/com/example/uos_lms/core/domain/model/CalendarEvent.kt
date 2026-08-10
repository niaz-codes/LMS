package com.example.uos_lms.core.domain.model

enum class CalendarEventType {
    HOLIDAY, EVENT, EXAM;

    companion object {
        fun fromStringOrNull(raw: String?): CalendarEventType? = entries.find { it.name == raw }
    }
}

data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String,
    val type: CalendarEventType,
    val dateMillis: Long,
    val createdBy: String,
    val createdAt: Long = 0L,
)
