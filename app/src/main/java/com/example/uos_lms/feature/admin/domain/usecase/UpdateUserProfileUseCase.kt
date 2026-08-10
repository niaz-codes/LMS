package com.example.uos_lms.feature.admin.domain.usecase

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(
        uid: String,
        fullName: String,
        fatherName: String,
        phone: String,
        cnic: String,
    ): AppResult<Unit> = repository.updateUserProfile(uid, fullName, fatherName, phone, cnic)
}
