package org.muilab.noti.summary.util

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.full.memberProperties

data class Summary (
    val userId: String,
    val timestamp: Long,
    val scheduled: Boolean,
    val prompt: String,
    val rating: Short,
    val notiScope: NotiScope,
    val notifications: List<SummaryNoti>,
    val summaryLength: WordCount
) {
    val dateTime: String
    init {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        dateTime = dateFormat.format(Date(timestamp))
    }
    data class NotiScope (
        val appName: Boolean,
        val time: Boolean,
        val title: Boolean,
        val content: Boolean
    )
    data class WordCount (
        val english: Int,
        val chinese: Int,
        val japanese: Int,
    )
    data class SummaryNoti (
        val appName: String,
        val postTime: Long,
        val wordCount: WordCount
    )
}

data class PromptAction (
    val userId: String,
    val timestamp: Long,
    val action: String,
    val history: Map<String, String>,
    val newPrompt: String
)

data class Scheduler(
    val userId: String,
    val timestamp: Long,
    val action: String,
    val scheduleTime: String,
    // val dayOfWeek: Int
)

data class AppScope (val userId: String, val timestamp: Long, val apps: Map<String, Boolean>)

inline fun <reified T : Any> T.extractVariables(): Pair<String, String>? {
    val userId = T::class.memberProperties.find { it.name == "userId" }
    val timestamp = T::class.memberProperties.find { it.name == "timestamp" }

    if (userId != null && timestamp != null)
        return Pair(userId.get(this).toString(), timestamp.get(this).toString())

    return null
}

inline fun <reified T : Any> uploadData(documentSet: String, document: T) {

    GlobalScope.launch {

        val db = Firebase.firestore
        val (userId, timestamp) = document.extractVariables() ?: Pair(String, String)
        val documentId = "${userId}_$timestamp"

        db.collection(documentSet)
            .document(documentId)
            .set(document)
            .addOnSuccessListener {
                Log.d(TAG, "Upload to set $documentSet with ID $documentId")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding context to Firestore", e)
            }

    }
}