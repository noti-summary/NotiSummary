package org.muilab.noti.summary.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.zqc.opencc.android.lib.ChineseConverter
import com.zqc.opencc.android.lib.ConversionType
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
import org.muilab.noti.summary.service.NotiUnit
import org.muilab.noti.summary.view.home.SummaryResponse
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit

class SummaryViewModel(application: Application): AndroidViewModel(application) {
    private val sharedPreferences = getApplication<Application>().getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)
    private val apiPref = getApplication<Application>().getSharedPreferences("ApiPref", Context.MODE_PRIVATE)

    private val _notifications = MutableLiveData<List<NotiUnit>>()
    val notifications: LiveData<List<NotiUnit>> = _notifications

    private val _result = MutableLiveData<String>()
    val result: LiveData<String> = _result
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext

    private var prompt = "Summarize the notifications in a Traditional Chinese statement."

    private val dotenv = dotenv {
        directory = "./assets"
        filename = "env"
    }
    private val serverURL = dotenv["SUMMARY_URL"]
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    init {
        val resultValue = sharedPreferences.getString("resultValue", SummaryResponse.HINT.message)
        _result.value = resultValue!!
    }

    private fun updateLiveDataValue(newValue: String?) {
        if (newValue != null) {
            _result.postValue(newValue!!)
            val editor = sharedPreferences.edit()
            editor.putString("resultValue", newValue)
            editor.apply()

            val userAPIKey = sharedPreferences.getString("userAPIKey", "default")!!
            if (userAPIKey == "default") {
                subtractCredit(1)
            }

        } else {
            _result.postValue(SummaryResponse.SERVER_ERROR.message)
        }
    }

    private fun getPostContent(activeNotifications: ArrayList<NotiUnit>): String {
        val sb = StringBuilder()
        activeNotifications.shuffle()
        activeNotifications.forEach {

            val appName = it.appName
            val time = it.time
            val title = it.title
            val content = it.content

            sb.append("App: $appName, Time: $time, Title: $title, Content: $content\n")
            // sb.append("[App] $appName\n[Time] $time\n[Title] $title\n[Content] $content\n\n")
        }
        return sb.toString()
    }

    private fun levenshteinDistance(str1: String, str2: String): Int {
        val m = str1.length
        val n = str2.length

        val distanceMatrix = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m)
            distanceMatrix[i][0] = i

        for (j in 0..n)
            distanceMatrix[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1

                distanceMatrix[i][j] = minOf(
                    distanceMatrix[i - 1][j] + 1,
                    distanceMatrix[i][j - 1] + 1,
                    distanceMatrix[i - 1][j - 1] + cost
                )
            }
        }

        return distanceMatrix[m][n]
    }

    fun getSummaryText(curPrompt: String) {
        prompt = curPrompt
        val intent = Intent("edu.mui.noti.summary.REQUEST_ALLNOTIS")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun updateSummaryText(activeNotifications: ArrayList<NotiUnit>) {
        if (activeNotifications.size > 0){
            _result.postValue(SummaryResponse.GENERATING.message)
            val postContent = getPostContent(activeNotifications)
            _notifications.postValue(activeNotifications.toList())
            viewModelScope.launch {
                sendToServer(postContent)
            }
        } else {
            _result.postValue(SummaryResponse.NO_NOTIFICATION.message)
        }
    }

    private suspend fun sendToServer(content: String) = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .callTimeout(180, TimeUnit.SECONDS)
            .build()

        Log.d("sendToServer@SummaryViewModel", "current prompt: $prompt")
        data class GPTRequest(val prompt: String, val content: String)
        data class GPTRequestWithKey(val prompt: String, val content: String, val key: String)

        val userAPIKey = apiPref.getString("userAPIKey", "系統金鑰")!!

        val requestURL = if(userAPIKey == "系統金鑰") {
            serverURL
        } else {
            "$serverURL/key"
        }

        @Suppress("IMPLICIT_CAST_TO_ANY")
        val gptRequest = if(userAPIKey == "系統金鑰") {
            GPTRequest(prompt, content)
        } else {
            Log.d("sendToServer", "userAPIKey: $userAPIKey")
            GPTRequestWithKey(prompt, content, userAPIKey)
        }

        val postBody = Gson().toJson(gptRequest)

        val request = Request.Builder()
            .url(requestURL)
            .post(postBody.toRequestBody(mediaType))
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseText = response.body?.string()?.replace("\\n", "\r\n")?.removeSurrounding("\"")
                val summary = ChineseConverter.convert(responseText, ConversionType.S2TWP, context)
                updateLiveDataValue(summary)
            } else {
                response.body?.let {
                    val responseBody = it.string()
                    Log.i("ServerResponse", responseBody)
                    if (responseBody.contains("You didn't provide an API key") || responseBody.contains("Incorrect API key provided")) {
                        _result.postValue(SummaryResponse.APIKEY_ERROR.message)
                    } else if (responseBody.contains("exceeded your current quota")) {
                        _result.postValue(SummaryResponse.QUOTA_ERROR.message)
                    } else {
                        _result.postValue(SummaryResponse.SERVER_ERROR.message)
                    }
                } ?:let {
                    _result.postValue(SummaryResponse.SERVER_ERROR.message)
                }
            }
            response.close()
        } catch (e: InterruptedIOException) {
            Log.i("InterruptedIOException", e.toString())
            _result.postValue(SummaryResponse.TIMEOUT_ERROR.message)
        } catch (e: IOException) {
            Log.i("IOException", e.toString())
            _result.postValue(SummaryResponse.NETWORK_ERROR.message)
        } finally {
            Log.i("sendToServer", "Some Unknown Exception")
            _result.postValue(SummaryResponse.SERVER_ERROR.message)
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