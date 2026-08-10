package com.example.uos_lms.feature.auth.domain.usecase

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.feature.auth.domain.model.RegisterRequest
import com.example.uos_lms.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(request: RegisterRequest, onProgress: (Int) -> Unit = {}): AppResult<Unit> =
        repository.register(request, onProgress)
}
