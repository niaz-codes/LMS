package com.example.uos_lms.feature.admin.domain.usecase

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.UserStatus
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import javax.inject.Inject

class UpdateUserStatusUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(uid: String, newStatus: UserStatus): AppResult<Unit> =
        repository.updateUserStatus(uid, newStatus)
}
