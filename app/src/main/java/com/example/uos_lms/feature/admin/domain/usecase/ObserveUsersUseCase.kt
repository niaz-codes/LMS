package com.example.uos_lms.feature.admin.domain.usecase

import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUsersUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    operator fun invoke(): Flow<List<User>> = repository.observeUsers()
}
