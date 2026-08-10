package com.example.uos_lms.core.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Turns any Firestore query into a live [Flow], remapping each snapshot's
 * documents through [mapper] and dropping any that fail to parse. Shared by
 * every `Firestore*DataSource` so the listener/close/awaitClose boilerplate
 * exists exactly once.
 */
internal fun <T> Query.observeAs(mapper: (DocumentSnapshot) -> T?): Flow<List<T>> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        trySend(snapshot?.documents.orEmpty().mapNotNull(mapper))
    }
    awaitClose { registration.remove() }
}
