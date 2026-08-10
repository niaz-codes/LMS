package com.example.uos_lms.feature.calendar.data.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.common.safeCall
import com.example.uos_lms.core.data.remote.FirestoreCalendarDataSource
import com.example.uos_lms.core.domain.model.CalendarEvent
import com.example.uos_lms.core.domain.model.CalendarEventType
import com.example.uos_lms.feature.calendar.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreCalendarDataSource,
) : CalendarRepository {

    override fun observeEvents(): Flow<List<CalendarEvent>> = dataSource.observeEvents()

    override suspend fun createEvent(
        title: String,
        description: String,
        type: CalendarEventType,
        dateMillis: Long,
        createdBy: String,
    ): AppResult<Unit> = safeCall {
        if (title.isBlank()) {
            error("Title is required.")
        }
        dataSource.createEvent(
            CalendarEvent(
                id = "",
                title = title,
                description = description,
                type = type,
                dateMillis = dateMillis,
                createdBy = createdBy,
            ),
        )
    }

    override suspend fun deleteEvent(id: String): AppResult<Unit> = safeCall {
        dataSource.deleteEvent(id)
    }
}
