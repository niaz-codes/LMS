package com.example.uos_lms.core.data.remote.dto

import com.example.uos_lms.core.domain.model.CalendarEvent
import com.example.uos_lms.core.domain.model.CalendarEventType
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CalendarEventDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = CalendarEventType.EVENT.name,
    val dateMillis: Long = 0L,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Date? = null,
) {
    fun toDomain() = CalendarEvent(
        id = id,
        title = title,
        description = description,
        type = CalendarEventType.fromStringOrNull(type) ?: CalendarEventType.EVENT,
        dateMillis = dateMillis,
        createdBy = createdBy,
        createdAt = createdAt?.time ?: 0L,
    )
}

fun CalendarEvent.toCreateMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "description" to description,
    "type" to type.name,
    "dateMillis" to dateMillis,
    "createdBy" to createdBy,
    "createdAt" to FieldValue.serverTimestamp(),
)
