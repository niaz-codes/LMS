package com.example.uos_lms.feature.attendance.data.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.common.safeCall
import com.example.uos_lms.core.data.remote.FirestoreAttendanceDataSource
import com.example.uos_lms.core.data.remote.FirestoreUserDataSource
import com.example.uos_lms.core.domain.model.AttendanceRecord
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceDataSource: FirestoreAttendanceDataSource,
    private val userDataSource: FirestoreUserDataSource,
) : AttendanceRepository {

    override fun observeStudentsInSemester(departmentId: String, semesterId: String): Flow<List<User>> =
        userDataSource.observeStudentsInSemester(departmentId, semesterId)

    override fun observeSessionAttendance(subjectId: String, dateKey: String): Flow<List<AttendanceRecord>> =
        attendanceDataSource.observeSessionAttendance(subjectId, dateKey)

    override fun observeHistoryForSubject(subjectId: String): Flow<List<AttendanceRecord>> =
        attendanceDataSource.observeHistoryForSubject(subjectId)

    override fun observeStudentAttendanceForSubject(subjectId: String, studentUid: String): Flow<List<AttendanceRecord>> =
        attendanceDataSource.observeStudentAttendanceForSubject(subjectId, studentUid)

    override fun observeAttendanceForDepartment(departmentId: String): Flow<List<AttendanceRecord>> =
        attendanceDataSource.observeAttendanceForDepartment(departmentId)

    override fun observeAttendanceForSemester(departmentId: String, semesterId: String): Flow<List<AttendanceRecord>> =
        attendanceDataSource.observeAttendanceForSemester(departmentId, semesterId)

    override suspend fun saveAttendance(records: List<AttendanceRecord>): AppResult<Unit> = safeCall {
        attendanceDataSource.saveAttendance(records)
    }
}
