package com.example.uos_lms.feature.material.domain.repository

import android.net.Uri
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.MaterialType
import com.example.uos_lms.core.domain.model.StudyMaterial
import kotlinx.coroutines.flow.Flow

interface MaterialRepository {
    fun observeMaterialsForSubject(subjectId: String): Flow<List<StudyMaterial>>

    suspend fun uploadMaterial(
        subjectId: String,
        departmentId: String,
        semesterId: String,
        title: String,
        materialType: MaterialType,
        fileUri: Uri,
        uploadedBy: String,
        uploadedByName: String,
        onProgress: (Int) -> Unit = {},
    ): AppResult<Unit>

    suspend fun deleteMaterial(materialId: String): AppResult<Unit>
}
