package com.example.uos_lms.core.domain.model

enum class AnnouncementScope {
    ALL, DEPARTMENT, SUBJECT;

    companion object {
        fun fromStringOrNull(raw: String?): AnnouncementScope? = entries.find { it.name == raw }
    }
}

data class Announcement(
    val id: String,
    val title: String,
    val body: String,
    val authorUid: String,
    val authorName: String,
    val scope: AnnouncementScope,
    val departmentId: String? = null,
    val departmentName: String? = null,
    val subjectId: String? = null,
    val subjectName: String? = null,
    val createdAt: Long = 0L,
)
