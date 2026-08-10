package com.example.uos_lms.feature.university.data.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.common.safeCall
import com.example.uos_lms.core.data.remote.FirestoreUniversityDataSource
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.domain.model.Session
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UniversityRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreUniversityDataSource,
) : UniversityRepository {

    override fun observeDepartments(): Flow<List<Department>> = dataSource.observeDepartments()

    override suspend fun createDepartment(name: String, code: String, description: String): AppResult<Unit> = safeCall {
        if (dataSource.isDepartmentCodeTaken(code)) {
            error("A department with code \"$code\" already exists.")
        }
        dataSource.createDepartment(name, code, description)
    }

    override suspend fun updateDepartment(
        id: String,
        name: String,
        code: String,
        description: String,
    ): AppResult<Unit> = safeCall {
        if (dataSource.isDepartmentCodeTaken(code, excludeId = id)) {
            error("A department with code \"$code\" already exists.")
        }
        dataSource.updateDepartment(id, name, code, description)
    }

    override suspend fun deleteDepartment(id: String): AppResult<Unit> = safeCall {
        dataSource.deleteDepartment(id)
    }

    override fun observeSemesters(departmentId: String): Flow<List<Semester>> =
        dataSource.observeSemesters(departmentId)

    override suspend fun createSemester(departmentId: String, number: Int): AppResult<Unit> = safeCall {
        if (dataSource.isSemesterNumberTaken(departmentId, number)) {
            error("Semester $number already exists in this department.")
        }
        dataSource.createSemester(departmentId, number)
    }

    override suspend fun deleteSemester(id: String): AppResult<Unit> = safeCall {
        dataSource.deleteSemester(id)
    }

    override fun observeSessionsForDepartment(departmentId: String): Flow<List<Session>> =
        dataSource.observeSessionsForDepartment(departmentId)

    override suspend fun createSession(departmentId: String, label: String): AppResult<Unit> = safeCall {
        val trimmed = label.trim()
        if (trimmed.isBlank()) {
            error("Session label is required.")
        }
        if (dataSource.isSessionLabelTaken(departmentId, trimmed)) {
            error("A session with label \"$trimmed\" already exists in this department.")
        }
        dataSource.createSession(departmentId, trimmed)
    }

    override suspend fun updateSessionLabel(id: String, departmentId: String, label: String): AppResult<Unit> = safeCall {
        val trimmed = label.trim()
        if (trimmed.isBlank()) {
            error("Session label is required.")
        }
        if (dataSource.isSessionLabelTaken(departmentId, trimmed, excludeId = id)) {
            error("A session with label \"$trimmed\" already exists in this department.")
        }
        dataSource.updateSessionLabel(id, trimmed)
    }

    override suspend fun setSessionActive(id: String, isActive: Boolean): AppResult<Unit> = safeCall {
        dataSource.setSessionActive(id, isActive)
    }

    override suspend fun deleteSession(id: String): AppResult<Unit> = safeCall {
        dataSource.deleteSession(id)
    }

    override suspend fun countAllSemesters(): AppResult<Long> = safeCall {
        dataSource.countAllSemesters()
    }

    override suspend fun countAllSessions(): AppResult<Long> = safeCall {
        dataSource.countAllSessions()
    }

    override fun observeSubjects(departmentId: String, semesterId: String): Flow<List<Subject>> =
        dataSource.observeSubjects(departmentId, semesterId)

    override fun observeSubjectsForTeacher(teacherUid: String): Flow<List<Subject>> =
        dataSource.observeSubjectsForTeacher(teacherUid)

    override fun observeAllSubjects(): Flow<List<Subject>> = dataSource.observeAllSubjects()

    override suspend fun createSubject(
        departmentId: String,
        semesterId: String,
        code: String,
        title: String,
        creditHours: Int,
    ): AppResult<Unit> = safeCall {
        if (dataSource.isSubjectCodeTaken(departmentId, code)) {
            error("A subject with code \"$code\" already exists in this department.")
        }
        dataSource.createSubject(departmentId, semesterId, code, title, creditHours)
    }

    override suspend fun updateSubject(
        id: String,
        departmentId: String,
        semesterId: String,
        code: String,
        title: String,
        creditHours: Int,
    ): AppResult<Unit> = safeCall {
        if (dataSource.isSubjectCodeTaken(departmentId, code, excludeId = id)) {
            error("A subject with code \"$code\" already exists in this department.")
        }
        dataSource.updateSubject(id, departmentId, semesterId, code, title, creditHours)
    }

    override suspend fun deleteSubject(id: String): AppResult<Unit> = safeCall {
        dataSource.deleteSubject(id)
    }

    override suspend fun assignTeacherToSubject(
        subjectId: String,
        teacherUid: String?,
        teacherName: String?,
    ): AppResult<Unit> = safeCall {
        dataSource.assignTeacher(subjectId, teacherUid, teacherName)
    }
}
