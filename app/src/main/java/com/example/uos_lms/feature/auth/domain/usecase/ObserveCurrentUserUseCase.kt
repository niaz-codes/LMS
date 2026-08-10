package com.example.uos_lms.feature.auth.domain.usecase

import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<User?> = repository.observeCurrentUserProfile()
}
