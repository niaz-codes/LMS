package com.example.uos_lms.feature.auth.domain.usecase

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RemoveProfilePhotoUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(uid: String): AppResult<Unit> = repository.removeProfilePhoto(uid)
}
