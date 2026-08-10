package com.example.uos_lms.core.data.remote

import com.example.uos_lms.core.data.remote.dto.DepartmentDto
import com.example.uos_lms.core.data.remote.dto.SemesterDto
import com.example.uos_lms.core.data.remote.dto.SessionDto
import com.example.uos_lms.core.data.remote.dto.SubjectDto
import com.example.uos_lms.core.data.remote.dto.toCreateMap
import com.example.uos_lms.core.data.remote.dto.toUpdateMap
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.domain.model.Session
import com.example.uos_lms.core.domain.model.Subject
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUniversityDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val departmentsCollection get() = firestore.collection("departments")
    private val semestersCollection get() = firestore.collection("semesters")
    private val subjectsCollection get() = firestore.collection("subjects")
    private val sessionsCollection get() = firestore.collection("sessions")

    // ---- Departments ----

    fun observeDepartments(): Flow<List<Department>> = departmentsCollection.observeAs { it.toObject(DepartmentDto::class.java)?.toDomain() }

    suspend fun isDepartmentCodeTaken(code: String, excludeId: String? = null): Boolean {
        val snapshot = departmentsCollection.whereEqualTo("code", code).get().await()
        return snapshot.documents.any { it.id != excludeId }
    }

    suspend fun createDepartment(name: String, code: String, description: String) {
        val ref = departmentsCollection.document()
        val department = Department(id = ref.id, name = name, code = code, description = description)
        ref.set(department.toCreateMap()).await()
    }

    suspend fun updateDepartment(id: String, name: String, code: String, description: String) {
        val department = Department(id = id, name = name, code = code, description = description)
        departmentsCollection.document(id).update(department.toUpdateMap()).await()
    }

    suspend fun deleteDepartment(id: String) {
        val hasSemesters = semestersCollection.whereEqualTo("departmentId", id).limit(1).get().await().documents.isNotEmpty()
        if (hasSemesters) {
            error("Delete all semesters in this department first.")
        }
        departmentsCollection.document(id).delete().await()
    }

    // ---- Semesters ----

    fun observeSemesters(departmentId: String): Flow<List<Semester>> =
        semestersCollection.whereEqualTo("departmentId", departmentId)
            .observeAs { it.toObject(SemesterDto::class.java)?.toDomain() }

    suspend fun isSemesterNumberTaken(departmentId: String, number: Int): Boolean {
        val snapshot = semestersCollection
            .whereEqualTo("departmentId", departmentId)
            .whereEqualTo("number", number)
            .get().await()
        return snapshot.documents.isNotEmpty()
    }

    suspend fun createSemester(departmentId: String, number: Int) {
        val ref = semestersCollection.document()
        val semester = Semester(id = ref.id, departmentId = departmentId, number = number)
        ref.set(semester.toCreateMap()).await()
    }

    suspend fun deleteSemester(id: String) {
        val hasSubjects = subjectsCollection.whereEqualTo("semesterId", id).limit(1).get().await().documents.isNotEmpty()
        if (hasSubjects) {
            error("Delete all subjects in this semester first.")
        }
        semestersCollection.document(id).delete().await()
    }

    // ---- Sessions ----

    fun observeSessionsForDepartment(departmentId: String): Flow<List<Session>> =
        sessionsCollection.whereEqualTo("departmentId", departmentId)
            .observeAs { it.toObject(SessionDto::class.java)?.toDomain() }

    suspend fun isSessionLabelTaken(departmentId: String, label: String, excludeId: String? = null): Boolean {
        val snapshot = sessionsCollection
            .whereEqualTo("departmentId", departmentId)
            .whereEqualTo("label", label)
            .get().await()
        return snapshot.documents.any { it.id != excludeId }
    }

    suspend fun createSession(departmentId: String, label: String) {
        val ref = sessionsCollection.document()
        val session = Session(id = ref.id, departmentId = departmentId, label = label)
        ref.set(session.toCreateMap()).await()
    }

    suspend fun updateSessionLabel(id: String, label: String) {
        sessionsCollection.document(id).update(
            mapOf(
                "label" to label,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    suspend fun setSessionActive(id: String, isActive: Boolean) {
        sessionsCollection.document(id).update(
            mapOf(
                "isActive" to isActive,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    suspend fun deleteSession(id: String) {
        sessionsCollection.document(id).delete().await()
    }

    suspend fun countAllSemesters(): Long =
        semestersCollection.count().get(AggregateSource.SERVER).await().count

    suspend fun countAllSessions(): Long =
        sessionsCollection.count().get(AggregateSource.SERVER).await().count

    // ---- Subjects ----

    fun observeSubjects(departmentId: String, semesterId: String): Flow<List<Subject>> =
        subjectsCollection.whereEqualTo("departmentId", departmentId)
            .whereEqualTo("semesterId", semesterId)
            .observeAs { it.toObject(SubjectDto::class.java)?.toDomain() }

    fun observeSubjectsForTeacher(teacherUid: String): Flow<List<Subject>> =
        subjectsCollection.whereEqualTo("teacherUid", teacherUid)
            .observeAs { it.toObject(SubjectDto::class.java)?.toDomain() }

    fun observeAllSubjects(): Flow<List<Subject>> =
        subjectsCollection.observeAs { it.toObject(SubjectDto::class.java)?.toDomain() }

    suspend fun isSubjectCodeTaken(departmentId: String, code: String, excludeId: String? = null): Boolean {
        val snapshot = subjectsCollection
            .whereEqualTo("departmentId", departmentId)
            .whereEqualTo("code", code)
            .get().await()
        return snapshot.documents.any { it.id != excludeId }
    }

    suspend fun createSubject(departmentId: String, semesterId: String, code: String, title: String, creditHours: Int) {
        val ref = subjectsCollection.document()
        val subject = Subject(
            id = ref.id,
            departmentId = departmentId,
            semesterId = semesterId,
            code = code,
            title = title,
            creditHours = creditHours,
        )
        ref.set(subject.toCreateMap()).await()
    }

    suspend fun updateSubject(id: String, departmentId: String, semesterId: String, code: String, title: String, creditHours: Int) {
        val subject = Subject(
            id = id,
            departmentId = departmentId,
            semesterId = semesterId,
            code = code,
            title = title,
            creditHours = creditHours,
        )
        subjectsCollection.document(id).update(subject.toUpdateMap()).await()
    }

    suspend fun assignTeacher(subjectId: String, teacherUid: String?, teacherName: String?) {
        subjectsCollection.document(subjectId).update(
            mapOf(
                "teacherUid" to teacherUid,
                "teacherName" to teacherName,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    suspend fun deleteSubject(id: String) {
        subjectsCollection.document(id).delete().await()
    }
}
