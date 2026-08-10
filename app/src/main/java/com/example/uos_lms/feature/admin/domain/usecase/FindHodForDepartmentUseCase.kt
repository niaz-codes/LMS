package com.example.uos_lms.feature.admin.domain.usecase

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import javax.inject.Inject

class FindHodForDepartmentUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(departmentId: String): AppResult<User?> =
        repository.findHodForDepartment(departmentId)
}
