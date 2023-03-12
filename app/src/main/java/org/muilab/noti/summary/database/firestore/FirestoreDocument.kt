package org.muilab.noti.summary.database.firestore

import com.google.firebase.firestore.DocumentSnapshot

sealed class FirestoreDocument {
    data class Snapshot(val snapshot: DocumentSnapshot) : FirestoreDocument()
    data class Error(val exception: Exception) : FirestoreDocument()
    object Loading : FirestoreDocument()
}