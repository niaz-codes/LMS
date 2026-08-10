package com.example.uos_lms.feature.admin.domain.usecase

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import javax.inject.Inject

class UpdateUserIdentifiersUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(
        uid: String,
        employeeId: String?,
        registrationNumber: String?,
        rollNumber: String?,
        designation: String?,
    ): AppResult<Unit> = repository.updateUserIdentifiers(uid, employeeId, registrationNumber, rollNumber, designation)
}
