package com.example.uos_lms.feature.announcement.domain.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Announcement
import com.example.uos_lms.core.domain.model.AnnouncementScope
import kotlinx.coroutines.flow.Flow

interface AnnouncementRepository {
    fun observeAnnouncements(): Flow<List<Announcement>>

    suspend fun postAnnouncement(
        title: String,
        body: String,
        authorUid: String,
        authorName: String,
        scope: AnnouncementScope,
        departmentId: String?,
        departmentName: String?,
        subjectId: String?,
        subjectName: String?,
    ): AppResult<Unit>

    suspend fun deleteAnnouncement(id: String): AppResult<Unit>
}
