package com.example.uos_lms.feature.university.domain.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.domain.model.Session
import com.example.uos_lms.core.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface UniversityRepository {
    fun observeDepartments(): Flow<List<Department>>
    suspend fun createDepartment(name: String, code: String, description: String): AppResult<Unit>
    suspend fun updateDepartment(id: String, name: String, code: String, description: String): AppResult<Unit>
    suspend fun deleteDepartment(id: String): AppResult<Unit>

    fun observeSemesters(departmentId: String): Flow<List<Semester>>
    suspend fun createSemester(departmentId: String, number: Int): AppResult<Unit>
    suspend fun deleteSemester(id: String): AppResult<Unit>

    fun observeSessionsForDepartment(departmentId: String): Flow<List<Session>>
    suspend fun createSession(departmentId: String, label: String): AppResult<Unit>
    suspend fun updateSessionLabel(id: String, departmentId: String, label: String): AppResult<Unit>
    suspend fun setSessionActive(id: String, isActive: Boolean): AppResult<Unit>
    suspend fun deleteSession(id: String): AppResult<Unit>
    suspend fun countAllSemesters(): AppResult<Long>
    suspend fun countAllSessions(): AppResult<Long>

    fun observeSubjects(departmentId: String, semesterId: String): Flow<List<Subject>>
    fun observeSubjectsForTeacher(teacherUid: String): Flow<List<Subject>>
    fun observeAllSubjects(): Flow<List<Subject>>
    suspend fun createSubject(
        departmentId: String,
        semesterId: String,
        code: String,
        title: String,
        creditHours: Int,
    ): AppResult<Unit>
    suspend fun updateSubject(
        id: String,
        departmentId: String,
        semesterId: String,
        code: String,
        title: String,
        creditHours: Int,
    ): AppResult<Unit>
    suspend fun deleteSubject(id: String): AppResult<Unit>
    suspend fun assignTeacherToSubject(subjectId: String, teacherUid: String?, teacherName: String?): AppResult<Unit>
}
