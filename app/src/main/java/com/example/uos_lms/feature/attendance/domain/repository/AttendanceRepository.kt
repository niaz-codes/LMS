package com.example.uos_lms.feature.attendance.domain.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.AttendanceRecord
import com.example.uos_lms.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun observeStudentsInSemester(departmentId: String, semesterId: String): Flow<List<User>>
    fun observeSessionAttendance(subjectId: String, dateKey: String): Flow<List<AttendanceRecord>>
    fun observeHistoryForSubject(subjectId: String): Flow<List<AttendanceRecord>>
    fun observeStudentAttendanceForSubject(subjectId: String, studentUid: String): Flow<List<AttendanceRecord>>
    fun observeAttendanceForDepartment(departmentId: String): Flow<List<AttendanceRecord>>
    fun observeAttendanceForSemester(departmentId: String, semesterId: String): Flow<List<AttendanceRecord>>
    suspend fun saveAttendance(records: List<AttendanceRecord>): AppResult<Unit>
}
