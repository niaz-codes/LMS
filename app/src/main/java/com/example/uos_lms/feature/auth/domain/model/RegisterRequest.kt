package com.example.uos_lms.feature.auth.domain.model

import android.net.Uri
import com.example.uos_lms.core.domain.model.UserRole

data class RegisterRequest(
    val fullName: String,
    val fatherName: String,
    val cnic: String,
    val phone: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val photoUri: Uri?,
)
