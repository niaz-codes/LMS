package com.example.uos_lms.core.data.remote

import com.example.uos_lms.core.data.remote.dto.UserDto
import com.example.uos_lms.core.data.remote.dto.toFirestoreMap
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.domain.model.UserStatus
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val usersCollection get() = firestore.collection("users")
    private val cnicIndexCollection get() = firestore.collection("cnicIndex")
    private val phoneIndexCollection get() = firestore.collection("phoneIndex")

    suspend fun getUser(uid: String): User? {
        val snapshot = usersCollection.document(uid).get().await()
        if (!snapshot.exists()) return null
        return snapshot.toObject(UserDto::class.java)?.toDomain()
    }

    suspend fun isCnicRegistered(cnic: String): Boolean =
        cnicIndexCollection.document(cnic).get().await().exists()

    suspend fun isPhoneRegistered(phone: String): Boolean =
        phoneIndexCollection.document(phone).get().await().exists()

    suspend fun createPendingUser(user: User) {
        firestore.runTransaction { transaction ->
            val cnicRef = cnicIndexCollection.document(user.cnic)
            val phoneRef = phoneIndexCollection.document(user.phone)
            val userRef = usersCollection.document(user.uid)

            if (transaction.get(cnicRef).exists()) {
                throw IllegalStateException("This CNIC is already registered.")
            }
            if (transaction.get(phoneRef).exists()) {
                throw IllegalStateException("This phone number is already registered.")
            }

            transaction.set(userRef, user.toFirestoreMap())
            transaction.set(cnicRef, mapOf("uid" to user.uid))
            transaction.set(phoneRef, mapOf("uid" to user.uid))
            null
        }.await()
    }

    fun observeUsers(): Flow<List<User>> =
        usersCollection.observeAs { it.toObject(UserDto::class.java)?.toDomain() }

    /** Live single-doc listener so changes (e.g. a profile photo update) reflect
     * everywhere the current user is shown, without a manual refetch. */
    fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val registration = usersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val user = if (snapshot != null && snapshot.exists()) {
                snapshot.toObject(UserDto::class.java)?.toDomain()
            } else {
                null
            }
            trySend(user)
        }
        awaitClose { registration.remove() }
    }

    suspend fun updateStatus(uid: String, newStatus: UserStatus, adminUid: String) {
        val fieldPrefix = when (newStatus) {
            UserStatus.APPROVED -> "approved"
            UserStatus.REJECTED -> "rejected"
            UserStatus.SUSPENDED -> "suspended"
            UserStatus.PENDING -> null
        }
        val updates = mutableMapOf<String, Any?>(
            "status" to newStatus.name,
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        if (fieldPrefix != null) {
            updates["${fieldPrefix}At"] = FieldValue.serverTimestamp()
            updates["${fieldPrefix}By"] = adminUid
        }
        usersCollection.document(uid).update(updates).await()
    }

    suspend fun deleteUser(uid: String) {
        val user = getUser(uid) ?: return
        firestore.runBatch { batch ->
            batch.delete(usersCollection.document(uid))
            batch.delete(cnicIndexCollection.document(user.cnic))
            batch.delete(phoneIndexCollection.document(user.phone))
        }.await()
    }

    /** A teacher can now belong to several departments (`departmentIds`), but older
     * docs only ever wrote the legacy scalar `department` field — this merges both
     * so a teacher shows up under every department they're assigned to, old or new. */
    fun observeTeachersInDepartment(departmentId: String): Flow<List<User>> {
        val legacy = usersCollection
            .whereEqualTo("role", UserRole.TEACHER.name)
            .whereEqualTo("department", departmentId)
            .whereEqualTo("status", UserStatus.APPROVED.name)
            .observeAs { it.toObject(UserDto::class.java)?.toDomain() }
        val assigned = usersCollection
            .whereEqualTo("role", UserRole.TEACHER.name)
            .whereArrayContains("departmentIds", departmentId)
            .whereEqualTo("status", UserStatus.APPROVED.name)
            .observeAs { it.toObject(UserDto::class.java)?.toDomain() }
        return combine(legacy, assigned) { a, b -> (a + b).distinctBy { it.uid } }
    }

    fun observeUsersInDepartment(departmentId: String): Flow<List<User>> =
        usersCollection
            .whereEqualTo("department", departmentId)
            .observeAs { it.toObject(UserDto::class.java)?.toDomain() }

    fun observeStudentsInSemester(departmentId: String, semesterId: String): Flow<List<User>> =
        usersCollection
            .whereEqualTo("role", UserRole.STUDENT.name)
            .whereEqualTo("department", departmentId)
            .whereEqualTo("semester", semesterId)
            .whereEqualTo("status", UserStatus.APPROVED.name)
            .observeAs { it.toObject(UserDto::class.java)?.toDomain() }

    /** Department-scoped, role-scoped, ALL statuses — unlike observeTeachersInDepartment
     * (APPROVED-only, used by HOD's teacher-facing screens), Admin's hierarchical
     * HOD/Teacher Management needs to see pending/rejected/suspended users too.
     * TEACHER is multi-department (see observeTeachersInDepartment), so it merges the
     * legacy scalar `department` field with the new `departmentIds` array; HOD/STUDENT
     * stay single-department and query the scalar field only. */
    fun observeUsersInDepartmentByRole(departmentId: String, role: UserRole): Flow<List<User>> {
        if (role != UserRole.TEACHER) {
            return usersCollection
                .whereEqualTo("department", departmentId)
                .whereEqualTo("role", role.name)
                .observeAs { it.toObject(UserDto::class.java)?.toDomain() }
        }
        val legacy = usersCollection
            .whereEqualTo("department", departmentId)
            .whereEqualTo("role", role.name)
            .observeAs { it.toObject(UserDto::class.java)?.toDomain() }
        val assigned = usersCollection
            .whereArrayContains("departmentIds", departmentId)
            .whereEqualTo("role", role.name)
            .observeAs { it.toObject(UserDto::class.java)?.toDomain() }
        return combine(legacy, assigned) { a, b -> (a + b).distinctBy { it.uid } }
    }

    /** Same "all statuses" reasoning as observeUsersInDepartmentByRole above. */
    fun observeStudentsInSession(departmentId: String, sessionId: String, semesterId: String): Flow<List<User>> =
        usersCollection
            .whereEqualTo("role", UserRole.STUDENT.name)
            .whereEqualTo("department", departmentId)
            .whereEqualTo("sessionId", sessionId)
            .whereEqualTo("semester", semesterId)
            .observeAs { it.toObject(UserDto::class.java)?.toDomain() }

    suspend fun updateDepartment(uid: String, departmentId: String?) {
        usersCollection.document(uid).update(
            mapOf(
                "department" to departmentId,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    /** Teacher-only: writes the full multi-department assignment plus keeps the
     * legacy scalar `department` field mirrored to the first id, so any code still
     * reading `department` off a Teacher (display, older queries) stays correct. */
    suspend fun updateDepartments(uid: String, departmentIds: List<String>) {
        usersCollection.document(uid).update(
            mapOf(
                "departmentIds" to departmentIds,
                "department" to departmentIds.firstOrNull(),
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    suspend fun updateSemester(uid: String, semesterId: String?) {
        usersCollection.document(uid).update(
            mapOf(
                "semester" to semesterId,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    suspend fun updateProfile(uid: String, fullName: String, fatherName: String, phone: String, cnic: String) {
        usersCollection.document(uid).update(
            mapOf(
                "fullName" to fullName,
                "fatherName" to fatherName,
                "phone" to phone,
                "cnic" to cnic,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    suspend fun updateIdentifiers(
        uid: String,
        employeeId: String?,
        registrationNumber: String?,
        rollNumber: String?,
        designation: String?,
    ) {
        usersCollection.document(uid).update(
            mapOf(
                "employeeId" to employeeId,
                "registrationNumber" to registrationNumber,
                "rollNumber" to rollNumber,
                "designation" to designation,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    suspend fun updateSession(uid: String, sessionId: String?) {
        usersCollection.document(uid).update(
            mapOf(
                "sessionId" to sessionId,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    suspend fun updateRole(uid: String, role: UserRole) {
        usersCollection.document(uid).update(
            mapOf(
                "role" to role.name,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    suspend fun updateProfilePhoto(uid: String, photoUrl: String?, photoPublicId: String?) {
        usersCollection.document(uid).update(
            mapOf(
                "profilePhotoUrl" to photoUrl,
                "profilePhotoPublicId" to photoPublicId,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    // ---- Count-only aggregate queries for tree-view badges: server-side
    // counts, no documents downloaded, so collapsed nodes stay cheap. ----

    suspend fun countUsersByRole(role: UserRole): Long =
        usersCollection.whereEqualTo("role", role.name)
            .count().get(AggregateSource.SERVER).await().count

    /** TEACHER is multi-department, so a cheap single count() aggregate would double-count
     * a teacher matched by both the legacy `department` field and the new `departmentIds`
     * array — this fetches both matching sets and de-dupes by doc id instead. Other roles
     * stay single-department and use the server-side count() aggregate as before. */
    suspend fun countUsersInDepartmentByRole(departmentId: String, role: UserRole): Long {
        if (role != UserRole.TEACHER) {
            return usersCollection
                .whereEqualTo("department", departmentId)
                .whereEqualTo("role", role.name)
                .count().get(AggregateSource.SERVER).await().count
        }
        val legacyIds = usersCollection
            .whereEqualTo("department", departmentId)
            .whereEqualTo("role", role.name)
            .get().await().documents.map { it.id }
        val assignedIds = usersCollection
            .whereArrayContains("departmentIds", departmentId)
            .whereEqualTo("role", role.name)
            .get().await().documents.map { it.id }
        return (legacyIds + assignedIds).distinct().size.toLong()
    }

    suspend fun countStudentsInDepartmentSemester(departmentId: String, semesterId: String): Long =
        usersCollection
            .whereEqualTo("role", UserRole.STUDENT.name)
            .whereEqualTo("department", departmentId)
            .whereEqualTo("semester", semesterId)
            .count().get(AggregateSource.SERVER).await().count

    /** Session-level badge for the Department -> Session -> Semester -> Students hierarchy —
     * all semesters within one session, unlike countStudentsInDepartmentSemester above. */
    suspend fun countStudentsInDepartmentSession(departmentId: String, sessionId: String): Long =
        usersCollection
            .whereEqualTo("role", UserRole.STUDENT.name)
            .whereEqualTo("department", departmentId)
            .whereEqualTo("sessionId", sessionId)
            .count().get(AggregateSource.SERVER).await().count

    suspend fun countStudentsInSession(departmentId: String, sessionId: String, semesterId: String): Long =
        usersCollection
            .whereEqualTo("role", UserRole.STUDENT.name)
            .whereEqualTo("department", departmentId)
            .whereEqualTo("sessionId", sessionId)
            .whereEqualTo("semester", semesterId)
            .count().get(AggregateSource.SERVER).await().count

    /** Every semester combined — used to guard Session deletion regardless of which semester(s) its students are currently in. */
    suspend fun countStudentsBySessionId(sessionId: String): Long =
        usersCollection
            .whereEqualTo("role", UserRole.STUDENT.name)
            .whereEqualTo("sessionId", sessionId)
            .count().get(AggregateSource.SERVER).await().count

    /** Moves every student out of [fromSessionId] into [toSessionId] in one batch — the
     * resolution path offered before a Session with linked students can be deleted. */
    suspend fun reassignSessionStudents(fromSessionId: String, toSessionId: String) {
        val snapshot = usersCollection
            .whereEqualTo("role", UserRole.STUDENT.name)
            .whereEqualTo("sessionId", fromSessionId)
            .get().await()
        if (snapshot.isEmpty) return
        firestore.runBatch { batch ->
            snapshot.documents.forEach { doc ->
                batch.update(
                    doc.reference,
                    mapOf(
                        "sessionId" to toSessionId,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
            }
        }.await()
    }

    suspend fun findHodForDepartment(departmentId: String): User? {
        val snapshot = usersCollection
            .whereEqualTo("role", UserRole.HOD.name)
            .whereEqualTo("department", departmentId)
            .whereEqualTo("status", UserStatus.APPROVED.name)
            .limit(1)
            .get().await()
        return snapshot.documents.firstOrNull()?.toObject(UserDto::class.java)?.toDomain()
    }
}
