package com.example.uos_lms.feature.admin.data.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.common.safeCall
import com.example.uos_lms.core.data.remote.FirestoreUserDataSource
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.domain.model.UserStatus
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val userDataSource: FirestoreUserDataSource,
    private val auth: FirebaseAuth,
) : AdminRepository {

    override fun observeUsers(): Flow<List<User>> = userDataSource.observeUsers()

    override suspend fun getUserDetail(uid: String): AppResult<User> = safeCall {
        userDataSource.getUser(uid) ?: error("User not found.")
    }

    override suspend fun updateUserStatus(uid: String, newStatus: UserStatus): AppResult<Unit> = safeCall {
        val adminUid = auth.currentUser?.uid ?: error("Admin session expired. Please log in again.")
        userDataSource.updateStatus(uid, newStatus, adminUid)
    }

    override suspend fun deleteUser(uid: String): AppResult<Unit> = safeCall {
        userDataSource.deleteUser(uid)
    }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> = safeCall {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun assignDepartment(uid: String, departmentId: String?): AppResult<Unit> = safeCall {
        userDataSource.updateDepartment(uid, departmentId)
    }

    override suspend fun assignDepartments(uid: String, departmentIds: List<String>): AppResult<Unit> = safeCall {
        userDataSource.updateDepartments(uid, departmentIds)
    }

    override suspend fun findHodForDepartment(departmentId: String): AppResult<User?> = safeCall {
        userDataSource.findHodForDepartment(departmentId)
    }

    override suspend fun assignSemester(uid: String, semesterId: String?): AppResult<Unit> = safeCall {
        userDataSource.updateSemester(uid, semesterId)
    }

    override suspend fun updateUserProfile(
        uid: String,
        fullName: String,
        fatherName: String,
        phone: String,
        cnic: String,
    ): AppResult<Unit> = safeCall {
        if (fullName.isBlank()) {
            error("Full name is required.")
        }
        userDataSource.updateProfile(uid, fullName.trim(), fatherName.trim(), phone.trim(), cnic.trim())
    }

    override suspend fun updateUserRole(uid: String, role: UserRole): AppResult<Unit> = safeCall {
        if (role !in UserRole.REGISTERABLE_ROLES) {
            error("Role must be HOD, Teacher, or Student.")
        }
        userDataSource.updateRole(uid, role)
    }

    override fun observeUsersInDepartmentByRole(departmentId: String, role: UserRole): Flow<List<User>> =
        userDataSource.observeUsersInDepartmentByRole(departmentId, role)

    override fun observeStudentsInSession(departmentId: String, sessionId: String, semesterId: String): Flow<List<User>> =
        userDataSource.observeStudentsInSession(departmentId, sessionId, semesterId)

    override suspend fun updateUserIdentifiers(
        uid: String,
        employeeId: String?,
        registrationNumber: String?,
        rollNumber: String?,
        designation: String?,
    ): AppResult<Unit> = safeCall {
        userDataSource.updateIdentifiers(
            uid,
            employeeId?.trim()?.ifBlank { null },
            registrationNumber?.trim()?.ifBlank { null },
            rollNumber?.trim()?.ifBlank { null },
            designation?.trim()?.ifBlank { null },
        )
    }

    override suspend fun assignSession(uid: String, sessionId: String?): AppResult<Unit> = safeCall {
        userDataSource.updateSession(uid, sessionId)
    }

    override suspend fun countUsersByRole(role: UserRole): AppResult<Long> = safeCall {
        userDataSource.countUsersByRole(role)
    }

    override suspend fun countUsersInDepartmentByRole(departmentId: String, role: UserRole): AppResult<Long> = safeCall {
        userDataSource.countUsersInDepartmentByRole(departmentId, role)
    }

    override suspend fun countStudentsInDepartmentSemester(departmentId: String, semesterId: String): AppResult<Long> = safeCall {
        userDataSource.countStudentsInDepartmentSemester(departmentId, semesterId)
    }

    override suspend fun countStudentsInDepartmentSession(departmentId: String, sessionId: String): AppResult<Long> = safeCall {
        userDataSource.countStudentsInDepartmentSession(departmentId, sessionId)
    }

    override suspend fun countStudentsInSession(departmentId: String, sessionId: String, semesterId: String): AppResult<Long> = safeCall {
        userDataSource.countStudentsInSession(departmentId, sessionId, semesterId)
    }

    override suspend fun countStudentsBySessionId(sessionId: String): AppResult<Long> = safeCall {
        userDataSource.countStudentsBySessionId(sessionId)
    }

    override suspend fun reassignSessionStudents(fromSessionId: String, toSessionId: String): AppResult<Unit> = safeCall {
        userDataSource.reassignSessionStudents(fromSessionId, toSessionId)
    }
}
