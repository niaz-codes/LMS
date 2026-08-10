package com.example.uos_lms.feature.calendar.domain.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.CalendarEvent
import com.example.uos_lms.core.domain.model.CalendarEventType
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    fun observeEvents(): Flow<List<CalendarEvent>>

    suspend fun createEvent(
        title: String,
        description: String,
        type: CalendarEventType,
        dateMillis: Long,
        createdBy: String,
    ): AppResult<Unit>

    suspend fun deleteEvent(id: String): AppResult<Unit>
}
