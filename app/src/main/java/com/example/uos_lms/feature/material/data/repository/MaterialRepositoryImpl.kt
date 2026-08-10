package com.example.uos_lms.feature.material.data.repository

import android.net.Uri
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.common.safeCall
import com.example.uos_lms.core.data.remote.CloudinaryDataSource
import com.example.uos_lms.core.data.remote.FirestoreMaterialDataSource
import com.example.uos_lms.core.domain.model.MaterialType
import com.example.uos_lms.core.domain.model.StudyMaterial
import com.example.uos_lms.feature.material.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaterialRepositoryImpl @Inject constructor(
    private val materialDataSource: FirestoreMaterialDataSource,
    private val cloudinaryDataSource: CloudinaryDataSource,
) : MaterialRepository {

    override fun observeMaterialsForSubject(subjectId: String): Flow<List<StudyMaterial>> =
        materialDataSource.observeMaterialsForSubject(subjectId)

    override suspend fun uploadMaterial(
        subjectId: String,
        departmentId: String,
        semesterId: String,
        title: String,
        materialType: MaterialType,
        fileUri: Uri,
        uploadedBy: String,
        uploadedByName: String,
        onProgress: (Int) -> Unit,
    ): AppResult<Unit> = safeCall {
        if (title.isBlank()) {
            error("Title is required.")
        }
        val materialId = materialDataSource.newMaterialId()
        val file = cloudinaryDataSource.uploadMaterialFile(subjectId, materialId, fileUri, onProgress)
        val material = StudyMaterial(
            id = materialId,
            subjectId = subjectId,
            departmentId = departmentId,
            semesterId = semesterId,
            title = title,
            materialType = materialType,
            fileUrl = file.url,
            fileName = file.fileName,
            filePublicId = file.publicId,
            fileResourceType = file.resourceType,
            fileSize = file.bytes,
            uploadedBy = uploadedBy,
            uploadedByName = uploadedByName,
        )
        materialDataSource.uploadMaterial(material)
    }

    override suspend fun deleteMaterial(materialId: String): AppResult<Unit> = safeCall {
        val material = materialDataSource.getMaterial(materialId)
        if (material != null) {
            runCatching { cloudinaryDataSource.delete(material.filePublicId, material.fileResourceType) }
        }
        materialDataSource.deleteMaterial(materialId)
    }
}
