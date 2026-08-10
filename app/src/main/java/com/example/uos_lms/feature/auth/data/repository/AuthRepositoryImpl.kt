package com.example.uos_lms.feature.auth.data.repository

import android.net.Uri
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.common.safeCall
import com.example.uos_lms.core.data.remote.CloudinaryDataSource
import com.example.uos_lms.core.data.remote.FirestoreUserDataSource
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.domain.model.UserStatus
import com.example.uos_lms.feature.auth.data.remote.FirebaseAuthDataSource
import com.example.uos_lms.feature.auth.domain.model.RegisterRequest
import com.example.uos_lms.feature.auth.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val userDataSource: FirestoreUserDataSource,
    private val cloudinaryDataSource: CloudinaryDataSource,
) : AuthRepository {

    override suspend fun login(email: String, password: String, expectedRole: UserRole): AppResult<User> = safeCall {
        val firebaseUser = authDataSource.signIn(email.trim(), password)
        val profile = userDataSource.getUser(firebaseUser.uid)
        if (profile == null) {
            authDataSource.signOut()
            error("No profile found for this account. Please contact the administrator.")
        }
        if (profile.role != expectedRole) {
            // Enforce the selected role strictly, even with correct credentials — never
            // leave a role-mismatched session signed in, or the caller could still be
            // routed into a dashboard for a role they didn't pick.
            authDataSource.signOut()
            error("Invalid role selected. Please choose your correct role.")
        }
        if (profile.status != UserStatus.APPROVED) {
            // Never leave a non-approved user signed in — the UI gate alone is not
            // sufficient, since Firestore security rules are the real enforcement boundary.
            authDataSource.signOut()
        }
        profile
    }

    override suspend fun register(request: RegisterRequest, onProgress: (Int) -> Unit): AppResult<Unit> = safeCall {
        require(request.role in UserRole.REGISTERABLE_ROLES) { "Invalid role for registration." }

        if (userDataSource.isCnicRegistered(request.cnic)) {
            error("This CNIC is already registered.")
        }
        if (userDataSource.isPhoneRegistered(request.phone)) {
            error("This phone number is already registered.")
        }

        val firebaseUser = authDataSource.createAccount(request.email.trim(), request.password)
        val uid = firebaseUser.uid

        try {
            val photo = request.photoUri?.let { cloudinaryDataSource.uploadProfilePhoto(uid, it, onProgress) }
            val newUser = User(
                uid = uid,
                fullName = request.fullName,
                fatherName = request.fatherName,
                cnic = request.cnic,
                phone = request.phone,
                email = request.email.trim(),
                role = request.role,
                status = UserStatus.PENDING,
                profilePhotoUrl = photo?.url,
                profilePhotoPublicId = photo?.publicId,
            )
            userDataSource.createPendingUser(newUser)
        } catch (e: Exception) {
            // Avoid leaving an orphaned Auth account with no matching profile doc.
            runCatching { authDataSource.deleteCurrentAccount() }
            throw e
        }

        authDataSource.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String): AppResult<Unit> = safeCall {
        val trimmed = email.trim()
        if (trimmed.isBlank()) {
            error("Email is required.")
        }
        try {
            authDataSource.sendPasswordResetEmail(trimmed)
        } catch (e: FirebaseAuthInvalidUserException) {
            // Only thrown when the Firebase project has Email Enumeration Protection
            // disabled — with it enabled (the modern default), Firebase intentionally
            // reports success either way to prevent probing which emails are registered.
            error("No account found with this email address.")
        }
    }

    override suspend fun logout() {
        authDataSource.signOut()
    }

    override suspend fun getCurrentUserProfile(): AppResult<User?> = safeCall {
        val firebaseUser = authDataSource.currentUser ?: return@safeCall null
        userDataSource.getUser(firebaseUser.uid)
    }

    override fun observeCurrentUserProfile(): Flow<User?> {
        val uid = authDataSource.currentUser?.uid ?: return flowOf(null)
        return userDataSource.observeUser(uid)
    }

    override suspend fun updateProfilePhoto(uid: String, uri: Uri, onProgress: (Int) -> Unit): AppResult<String> = safeCall {
        // Deterministic public_id + overwrite=true means the new upload replaces the old
        // asset at the same Cloudinary path, so there's no separate old-file cleanup here.
        val photo = cloudinaryDataSource.uploadProfilePhoto(uid, uri, onProgress)
        userDataSource.updateProfilePhoto(uid, photo.url, photo.publicId)
        photo.url
    }

    override suspend fun removeProfilePhoto(uid: String): AppResult<Unit> = safeCall {
        runCatching { cloudinaryDataSource.deleteProfilePhoto(uid) }
        userDataSource.updateProfilePhoto(uid, null, null)
    }

    override suspend fun updateOwnProfile(
        uid: String,
        fullName: String,
        fatherName: String,
        phone: String,
        cnic: String,
    ): AppResult<Unit> = safeCall {
        if (fullName.isBlank()) {
            error("Full name is required.")
        }
        userDataSource.updateProfile(uid, fullName, fatherName, phone, cnic)
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): AppResult<Unit> = safeCall {
        if (newPassword.length < 6) {
            error("New password must be at least 6 characters.")
        }
        authDataSource.changePassword(oldPassword, newPassword)
    }
}
