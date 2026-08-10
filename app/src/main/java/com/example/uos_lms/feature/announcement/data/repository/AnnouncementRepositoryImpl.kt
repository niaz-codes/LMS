package com.example.uos_lms.feature.announcement.data.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.common.safeCall
import com.example.uos_lms.core.data.remote.FirestoreAnnouncementDataSource
import com.example.uos_lms.core.domain.model.Announcement
import com.example.uos_lms.core.domain.model.AnnouncementScope
import com.example.uos_lms.feature.announcement.domain.repository.AnnouncementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreAnnouncementDataSource,
) : AnnouncementRepository {

    override fun observeAnnouncements(): Flow<List<Announcement>> = dataSource.observeAnnouncements()

    override suspend fun postAnnouncement(
        title: String,
        body: String,
        authorUid: String,
        authorName: String,
        scope: AnnouncementScope,
        departmentId: String?,
        departmentName: String?,
        subjectId: String?,
        subjectName: String?,
    ): AppResult<Unit> = safeCall {
        if (title.isBlank()) {
            error("Title is required.")
        }
        if (body.isBlank()) {
            error("Message is required.")
        }
        dataSource.createAnnouncement(
            Announcement(
                id = "",
                title = title,
                body = body,
                authorUid = authorUid,
                authorName = authorName,
                scope = scope,
                departmentId = departmentId,
                departmentName = departmentName,
                subjectId = subjectId,
                subjectName = subjectName,
            ),
        )
    }

    override suspend fun deleteAnnouncement(id: String): AppResult<Unit> = safeCall {
        dataSource.deleteAnnouncement(id)
    }
}
