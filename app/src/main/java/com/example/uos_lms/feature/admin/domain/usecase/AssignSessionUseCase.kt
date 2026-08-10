package com.example.uos_lms.feature.admin.domain.usecase

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import javax.inject.Inject

class AssignSessionUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(uid: String, sessionId: String?): AppResult<Unit> =
        repository.assignSession(uid, sessionId)
}
