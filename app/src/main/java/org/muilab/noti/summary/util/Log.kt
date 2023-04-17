package org.muilab.noti.summary.util

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.full.memberProperties

data class SummaryNoti(
    val appName: String,
    val postTime: Long,
    val wordCount: Map<String, Int>
)

data class Summary(
    val userId: String,
    val timestamp: Long,
    val scheduled: Boolean,
    val prompt: String,
    val rating: Int,
    val notiScope: Map<String, Boolean>,
    val notifications: List<SummaryNoti>,
    val summaryLength: Map<String, Int>
) {
    val dateTime: String
    init {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        dateTime = dateFormat.format(Date(timestamp))
    }
}

fun saveSummary(context: Context, summary: Summary) {
    val gson = Gson()
    val summaryJson = gson.toJson(summary)
    val sharedPref = context.getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)
    sharedPref.edit().putString("summaryJson", summaryJson).apply()
}

fun loadSummary(context: Context): Summary? {
    val gson = Gson()
    val sharedPref = context.getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)
    val summaryJson = sharedPref.getString("summaryJson", null)
    return gson.fromJson(summaryJson, Summary::class.java)
}

data class PromptAction(
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

data class AppScope(val userId: String, val timestamp: Long, val apps: Map<String, Boolean>)

inline fun <reified T : Any> T.extractVariables(): Pair<String, String>? {
    val userId = T::class.memberProperties.find { it.name == "userId" }
    val timestamp = T::class.memberProperties.find { it.name == "timestamp" }

    if (userId != null && timestamp != null)
        return Pair(userId.get(this).toString(), timestamp.get(this).toString())

    return null
}

inline fun <reified T : Any> uploadData(documentSet: String, document: T) {

    val db = Firebase.firestore
    val (userId, timestamp) = document.extractVariables() ?: Pair(String, String)
    val documentId = "${userId}_$timestamp"

    db.collection(documentSet)
        .document(documentId)
        .set(document)
        .addOnSuccessListener {
            Log.d("Data Log", "Upload to set $documentSet with ID $documentId")
        }
        .addOnFailureListener { e ->
            Log.w("Data Log", "Error adding context to Firestore", e)
        }

}