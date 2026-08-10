package com.example.uos_lms.feature.admin.domain.usecase

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import javax.inject.Inject

class AssignSemesterUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(uid: String, semesterId: String?): AppResult<Unit> =
        repository.assignSemester(uid, semesterId)
}
