package com.example.uos_lms.core.data.remote

import com.example.uos_lms.core.data.remote.dto.AnnouncementDto
import com.example.uos_lms.core.data.remote.dto.toCreateMap
import com.example.uos_lms.core.domain.model.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreAnnouncementDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val announcementsCollection get() = firestore.collection("announcements")

    suspend fun createAnnouncement(announcement: Announcement) {
        val ref = announcementsCollection.document()
        ref.set(announcement.copy(id = ref.id).toCreateMap()).await()
    }

    suspend fun deleteAnnouncement(id: String) {
        announcementsCollection.document(id).delete().await()
    }

    fun observeAnnouncements(): Flow<List<Announcement>> =
        announcementsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .observeAs { it.toObject(AnnouncementDto::class.java)?.toDomain() }
}
