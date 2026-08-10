package com.example.uos_lms.feature.auth.domain.usecase

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): AppResult<User?> = repository.getCurrentUserProfile()
}
