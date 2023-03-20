package org.muilab.noti.summary.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONException
import org.muilab.noti.summary.model.UserCredit
import org.muilab.noti.summary.service.NotiItem
import org.muilab.noti.summary.service.NotiUnit
import java.util.concurrent.TimeUnit

class SummaryViewModel(application: Application): AndroidViewModel(application) {
    private val sharedPreferences = getApplication<Application>().getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)

    private val _notifications = MutableLiveData<List<NotiUnit>>()
    val notifications: LiveData<List<NotiUnit>> = _notifications

    private val _result = MutableLiveData<String>()
    val result: LiveData<String> = _result
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private val prompt = "Summarize the notifications in a Traditional Chinese statement."

    private val dotenv = dotenv {
        directory = "./assets"
        filename = "env"
    }
    private val serverIP = dotenv["SUMMARY_URL"]
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    init {
        val resultValue = sharedPreferences.getString("resultValue", "")
        if (resultValue != "") {
            _result.value = resultValue
        }
    }

    private fun updateLiveDataValue(newValue: String?) {
        if (newValue != null) {
            _result.postValue(newValue)
            val editor = sharedPreferences.edit()
            editor.putString("resultValue", newValue)
            editor.apply()

            subtractCredit(1)
        }
    }

    fun getPostContent(activeNotifications: ArrayList<NotiUnit>) : String {
        val sb = StringBuilder()
        val notiList = activeNotifications
        notiList.shuffle()
        notiList.forEach {

            val appName = it.appName
            val time = it.time
            val title = it.title
            val content = it.content

            sb.append("[App] $appName\n[Time] $time\n[Title] $title\n[Content] $content\n\n")
        }
        return sb.toString()
    }

    fun getSummaryText() {
        val intent = Intent("edu.mui.noti.summary.REQUEST_ALLNOTIS")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun updateSummaryText(activeNotifications: ArrayList<NotiUnit>) {
        _result.postValue("通知摘要產生中，請稍候...")
        val postContent = getPostContent(activeNotifications)
        _notifications.postValue(activeNotifications.toList())
        viewModelScope.launch {
            sendToServer(postContent)
        }
    }

    private suspend fun sendToServer(content: String) = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .build()

        data class GPTRequest(val prompt: String, val content: String)

        val gptRequest = GPTRequest(prompt, content)
        val postBody = Gson().toJson(gptRequest)
        val request = Request.Builder()
            .url(serverIP)
            .post(postBody.toRequestBody(mediaType))
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val summary = response.body?.string()?.replace("\\n", "\r\n")?.removeSurrounding("\"")
                updateLiveDataValue(summary)
            } else {
                response.body?.let { Log.i("Server", it.string()) }
            }
        } catch (e: IOException) {
            _result.postValue("無法連線...請確認網路連線！")
        } catch (e: JSONException) {
            _result.postValue(e.toString())
        }
    }

    private fun subtractCredit(number: Int) {
        val sharedPref = context.getSharedPreferences("user_id", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", "000").toString()

        val db = Firebase.firestore
        val docRef = db.collection("user-free-credit").document(userId)
        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val res = document.toObject<UserCredit>()!!
                docRef.update("credit", res.credit-number)
            }
        }
    }
}