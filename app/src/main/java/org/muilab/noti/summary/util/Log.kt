package org.muilab.noti.summary.util

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.muilab.noti.summary.database.room.UserActionDao
import org.muilab.noti.summary.database.room.UserActionDatabase
import org.muilab.noti.summary.model.UserAction
import org.muilab.noti.summary.service.NotiUnit
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.full.memberProperties

data class SummaryNoti(
    val sbnKey: String,
    val `when`: Long,
    val postTime: Long,
    val pkgName: String,
    val category: String,
    val titleWordCount: Map<String, Int>,
    val contentWordCount: Map<String, Int>
) {
    constructor(notiUnit: NotiUnit): this (
        sbnKey = notiUnit.sbnKey,
        `when` = notiUnit.`when`,
        postTime = notiUnit.postTime,
        pkgName = notiUnit.pkgName,
        category = notiUnit.category,
        titleWordCount = getWordCount(notiUnit.title),
        contentWordCount = getWordCount(notiUnit.content)
    )
}

data class Summary(
    val userId: String,
    val timestamp: Long,
    val scheduled: Boolean,
    val prompt: String,
    val rating: Int,
    val notiScope: Map<String, Boolean>,
    val notifications: List<SummaryNoti>,
    val summaryLength: Map<String, Int>,
    val removedNotis: Map<String, String>
) {
    val dateTime: String
    init {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        dateTime = dateFormat.format(Date(timestamp))
    }
}

fun getWordCount(content: String): Map<String, Int> {
    val latinRegex = "[\\p{IsLatin}\\p{Punct}]+".toRegex()
    val chineseRegex = "[\\p{InCJKUnifiedIdeographs}\\p{Punct}]+".toRegex()
    val japaneseRegex = "[\\p{InHiragana}\\p{InKatakana}\\p{InCJKUnifiedIdeographs}\\p{InHalfwidthAndFullwidthForms}\\p{Punct}]+".toRegex()

    val latinStrings = mutableListOf<String>()
    val chineseStrings = mutableListOf<String>()
    val japaneseStrings = mutableListOf<String>()
    val otherStrings = mutableListOf<String>()

    var remainingContent = content
    while (remainingContent.isNotEmpty()) {
        // Find the first occurrence of each language category in the remaining input string
        val latinMatch = latinRegex.find(remainingContent)
        val chineseMatch = chineseRegex.find(remainingContent)
        val japaneseMatch = japaneseRegex.find(remainingContent)

        var nextMatch: MatchResult? = null
        var nextLanguage: String? = null
        if (latinMatch != null && (nextMatch == null || latinMatch.range.first < nextMatch.range.first)) {
            nextMatch = latinMatch
            nextLanguage = "Latin"
        }
        if (chineseMatch != null && (nextMatch == null || chineseMatch.range.first < nextMatch.range.first)) {
            nextMatch = chineseMatch
            nextLanguage = "Chinese"
        }
        if (japaneseMatch != null && (nextMatch == null || japaneseMatch.range.first < nextMatch.range.first)) {
            nextMatch = japaneseMatch
            nextLanguage = "Japanese"
        }

        if (nextMatch != null && nextLanguage != null) {
            val currentPart = remainingContent.substring(0, nextMatch.range.last + 1)
            when (nextLanguage) {
                "Latin" -> latinStrings.add(currentPart)
                "Chinese" -> chineseStrings.add(currentPart)
                "Japanese" -> japaneseStrings.add(currentPart)
                else -> otherStrings.add(currentPart)
            }
            remainingContent = remainingContent.substring(nextMatch.range.last + 1)
        } else {
            otherStrings.add(remainingContent)
            break
        }
    }

    val wordCount = mutableMapOf<String, Int>()
    val latinWordCount = latinStrings.fold(0) { sum, str ->
        val strWordCount = str.split(' ').fold(0) { wordSum, wordStr ->
            wordSum + if (wordStr.isNotEmpty()) 1 else 0
        }
        sum + strWordCount
    }
    val chineseWordCount = chineseStrings.fold(0) { sum, str ->
        val strWordCount = str.fold(0) { wordSum, ch ->
            wordSum + if (!ch.equals("") && !ch.equals(" ")) 1 else 0
        }
        sum + strWordCount
    }
    val japaneseWordCount = japaneseStrings.fold(0) { sum, str ->
        val strWordCount = str.fold(0) { wordSum, ch ->
            wordSum + if (!ch.equals("") && !ch.equals(" ")) 1 else 0
        }
        sum + strWordCount
    }
    val otherWordCount = otherStrings.fold(0) { sum, str ->
        val strWordCount = str.fold(0) { wordSum, ch ->
            wordSum + if (!ch.equals("") && !ch.equals(" ")) 1 else 0
        }
        sum + strWordCount
    }
    wordCount["wordCount"] = latinWordCount + chineseWordCount + japaneseWordCount + otherWordCount
    wordCount["characters"] = content.length
    return wordCount
}

fun logSummary(context: Context) {

    val userSharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    val summarySharedPref = context.getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)

    val userId = userSharedPref.getString("user_id", "000").toString()

    val submitTime = summarySharedPref.getLong("submitTime", 0)
    val isScheduled = summarySharedPref.getBoolean("isScheduled", false)
    val prompt = summarySharedPref.getString("prompt", "").toString()

    val notiScopeJson = summarySharedPref.getString("notiScope", "")
    val notiScopeType = object : TypeToken<Map<String, Boolean>>() {}.type
    val notiScope = Gson().fromJson<Map<String, Boolean>>(notiScopeJson, notiScopeType)

    val notiDataJson = summarySharedPref.getString("notiData", "")
    val notiDataType = object : TypeToken<List<SummaryNoti>>() {}.type
    val notiData = Gson().fromJson<List<SummaryNoti>>(notiDataJson, notiDataType)

    val summaryLengthJson = summarySharedPref.getString("summaryLength", "")
    val summaryLengthType = object : TypeToken<Map<String, Int>>() {}.type
    val summaryLength = Gson().fromJson<Map<String, Int>>(summaryLengthJson, summaryLengthType)

    val removedNotisJson = summarySharedPref.getString("removedNotis", "{}")
    val removedNotisType = object : TypeToken<MutableMap<String, String>>() {}.type
    val removedNotis = Gson().fromJson<MutableMap<String, String>>(removedNotisJson, removedNotisType)

    val rating = summarySharedPref.getInt("rating", 0)

    val summary = Summary(
        userId,
        submitTime,
        isScheduled,
        prompt,
        rating,
        notiScope,
        notiData,
        summaryLength,
        removedNotis
    )
    uploadData("summary", summary)
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
    val daysOfWeek: Int,
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

fun logUserAction(type: String, actionName: String, context: Context, metadata: String = "") {

    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("user_id", "000").toString()
    val time = System.currentTimeMillis()
    val userAction = UserAction(userId, time, type, actionName, metadata)

    CoroutineScope(Dispatchers.IO).launch {
        val userActionDatabase = UserActionDatabase.getInstance(context)
        val userActionDao = userActionDatabase.userActionDao()
        userActionDao.insertAction(userAction)
        if (userActionDao.getActionsCount() >= 100 || type == "lifeCycle")
            uploadUserAction(userActionDao)
    }
}

fun uploadUserAction(userActionDao: UserActionDao) {

    Log.d("UserAction", "upload")

    val db = Firebase.firestore
    val batch = db.batch()

    val userActions = userActionDao.getAllActions()
    userActions.forEach { userAction ->
        val docRef = db.collection("userAction").document(userAction.primaryKey)
        batch.set(docRef, userAction)
    }
    batch.commit()
        .addOnSuccessListener {
            Log.d("UserAction", "Uploaded user actions to Firestore")
            CoroutineScope(Dispatchers.IO).launch {
                userActionDao.deleteAll()
            }
        }
        .addOnFailureListener {
            Log.d("UserAction", "Failed to upload to Firestore")
        }
}