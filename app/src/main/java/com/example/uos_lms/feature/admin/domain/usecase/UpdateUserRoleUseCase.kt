package com.example.uos_lms.feature.admin.domain.usecase

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import javax.inject.Inject

class UpdateUserRoleUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(uid: String, role: UserRole): AppResult<Unit> =
        repository.updateUserRole(uid, role)
}
