package com.example.uos_lms.core.data.remote.dto

import com.example.uos_lms.core.domain.model.Announcement
import com.example.uos_lms.core.domain.model.AnnouncementScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class AnnouncementDto(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val scope: String = AnnouncementScope.ALL.name,
    val departmentId: String? = null,
    val departmentName: String? = null,
    val subjectId: String? = null,
    val subjectName: String? = null,
    @ServerTimestamp val createdAt: Date? = null,
) {
    fun toDomain() = Announcement(
        id = id,
        title = title,
        body = body,
        authorUid = authorUid,
        authorName = authorName,
        scope = AnnouncementScope.fromStringOrNull(scope) ?: AnnouncementScope.ALL,
        departmentId = departmentId,
        departmentName = departmentName,
        subjectId = subjectId,
        subjectName = subjectName,
        createdAt = createdAt?.time ?: 0L,
    )
}

fun Announcement.toCreateMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "body" to body,
    "authorUid" to authorUid,
    "authorName" to authorName,
    "scope" to scope.name,
    "departmentId" to departmentId,
    "departmentName" to departmentName,
    "subjectId" to subjectId,
    "subjectName" to subjectName,
    "createdAt" to FieldValue.serverTimestamp(),
)
