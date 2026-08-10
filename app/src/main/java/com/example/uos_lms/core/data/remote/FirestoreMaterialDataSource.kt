package com.example.uos_lms.core.data.remote

import com.example.uos_lms.core.data.remote.dto.StudyMaterialDto
import com.example.uos_lms.core.data.remote.dto.toCreateMap
import com.example.uos_lms.core.domain.model.StudyMaterial
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreMaterialDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val materialsCollection get() = firestore.collection("materials")

    fun newMaterialId(): String = materialsCollection.document().id

    fun observeMaterialsForSubject(subjectId: String): Flow<List<StudyMaterial>> =
        materialsCollection.whereEqualTo("subjectId", subjectId)
            .observeAs { it.toObject(StudyMaterialDto::class.java)?.toDomain() }

    suspend fun uploadMaterial(material: StudyMaterial) {
        materialsCollection.document(material.id).set(material.toCreateMap()).await()
    }

    suspend fun getMaterial(materialId: String): StudyMaterial? {
        val snapshot = materialsCollection.document(materialId).get().await()
        if (!snapshot.exists()) return null
        return snapshot.toObject(StudyMaterialDto::class.java)?.toDomain()
    }

    suspend fun deleteMaterial(materialId: String) {
        materialsCollection.document(materialId).delete().await()
    }
}
