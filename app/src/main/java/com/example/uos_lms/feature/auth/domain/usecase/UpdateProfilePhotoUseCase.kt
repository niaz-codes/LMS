package com.example.uos_lms.feature.auth.domain.usecase

import android.net.Uri
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateProfilePhotoUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(uid: String, uri: Uri, onProgress: (Int) -> Unit = {}): AppResult<String> =
        repository.updateProfilePhoto(uid, uri, onProgress)
}
