package com.example.uos_lms.core.data.remote

import com.example.uos_lms.core.data.remote.dto.CalendarEventDto
import com.example.uos_lms.core.data.remote.dto.toCreateMap
import com.example.uos_lms.core.domain.model.CalendarEvent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCalendarDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val eventsCollection get() = firestore.collection("calendarEvents")

    suspend fun createEvent(event: CalendarEvent) {
        val ref = eventsCollection.document()
        ref.set(event.copy(id = ref.id).toCreateMap()).await()
    }

    suspend fun deleteEvent(id: String) {
        eventsCollection.document(id).delete().await()
    }

    fun observeEvents(): Flow<List<CalendarEvent>> =
        eventsCollection
            .orderBy("dateMillis", Query.Direction.ASCENDING)
            .observeAs { it.toObject(CalendarEventDto::class.java)?.toDomain() }
}
