package com.example.uos_lms.feature.admin.domain.usecase

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import javax.inject.Inject

class AssignDepartmentsUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(uid: String, departmentIds: List<String>): AppResult<Unit> =
        repository.assignDepartments(uid, departmentIds)
}
