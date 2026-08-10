package com.example.uos_lms.feature.admin.domain.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.domain.model.UserStatus
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun observeUsers(): Flow<List<User>>
    suspend fun getUserDetail(uid: String): AppResult<User>
    suspend fun updateUserStatus(uid: String, newStatus: UserStatus): AppResult<Unit>
    suspend fun deleteUser(uid: String): AppResult<Unit>
    suspend fun sendPasswordReset(email: String): AppResult<Unit>

    suspend fun assignDepartment(uid: String, departmentId: String?): AppResult<Unit>
    suspend fun assignDepartments(uid: String, departmentIds: List<String>): AppResult<Unit>
    suspend fun findHodForDepartment(departmentId: String): AppResult<User?>
    suspend fun assignSemester(uid: String, semesterId: String?): AppResult<Unit>
    suspend fun updateUserProfile(uid: String, fullName: String, fatherName: String, phone: String, cnic: String): AppResult<Unit>
    suspend fun updateUserRole(uid: String, role: UserRole): AppResult<Unit>

    fun observeUsersInDepartmentByRole(departmentId: String, role: UserRole): Flow<List<User>>
    fun observeStudentsInSession(departmentId: String, sessionId: String, semesterId: String): Flow<List<User>>
    suspend fun updateUserIdentifiers(
        uid: String,
        employeeId: String?,
        registrationNumber: String?,
        rollNumber: String?,
        designation: String?,
    ): AppResult<Unit>
    suspend fun assignSession(uid: String, sessionId: String?): AppResult<Unit>

    suspend fun countUsersByRole(role: UserRole): AppResult<Long>
    suspend fun countUsersInDepartmentByRole(departmentId: String, role: UserRole): AppResult<Long>
    suspend fun countStudentsInDepartmentSemester(departmentId: String, semesterId: String): AppResult<Long>
    suspend fun countStudentsInDepartmentSession(departmentId: String, sessionId: String): AppResult<Long>
    suspend fun countStudentsInSession(departmentId: String, sessionId: String, semesterId: String): AppResult<Long>
    suspend fun countStudentsBySessionId(sessionId: String): AppResult<Long>
    suspend fun reassignSessionStudents(fromSessionId: String, toSessionId: String): AppResult<Unit>
}
