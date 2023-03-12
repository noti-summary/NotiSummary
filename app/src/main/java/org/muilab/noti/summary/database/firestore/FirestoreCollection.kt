package org.muilab.noti.summary.database.firestore

import com.google.firebase.firestore.DocumentSnapshot

sealed class FirestoreCollection {
    data class Snapshot(val list: List<DocumentSnapshot>) : FirestoreCollection()
    data class Error(val exception: Exception) : FirestoreCollection()
    object Loading : FirestoreCollection()
}