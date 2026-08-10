package com.example.uos_lms.feature.auth.domain.repository

import android.net.Uri
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.feature.auth.domain.model.RegisterRequest
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String, expectedRole: UserRole): AppResult<User>
    suspend fun register(request: RegisterRequest, onProgress: (Int) -> Unit = {}): AppResult<Unit>
    suspend fun sendPasswordResetEmail(email: String): AppResult<Unit>
    suspend fun logout()
    suspend fun getCurrentUserProfile(): AppResult<User?>
    fun observeCurrentUserProfile(): Flow<User?>
    suspend fun updateProfilePhoto(uid: String, uri: Uri, onProgress: (Int) -> Unit = {}): AppResult<String>
    suspend fun removeProfilePhoto(uid: String): AppResult<Unit>
    suspend fun updateOwnProfile(uid: String, fullName: String, fatherName: String, phone: String, cnic: String): AppResult<Unit>
    suspend fun changePassword(oldPassword: String, newPassword: String): AppResult<Unit>
}
