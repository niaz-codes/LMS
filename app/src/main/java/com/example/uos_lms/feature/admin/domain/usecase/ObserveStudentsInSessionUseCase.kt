package com.example.uos_lms.feature.admin.domain.usecase

import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveStudentsInSessionUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    operator fun invoke(departmentId: String, sessionId: String, semesterId: String): Flow<List<User>> =
        repository.observeStudentsInSession(departmentId, sessionId, semesterId)
}
