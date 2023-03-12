package edu.mui.noti.summary.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import edu.mui.noti.summary.util.TAG
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SummaryViewModel(application: Application): AndroidViewModel(application) {
    private val _result = MutableLiveData<String>()
    val result: LiveData<String> = _result
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private val prompt = "你將摘要手機通知，請從以下通知(段落的先後順序不一定和重要性相關)篩選出重要資訊，並以一段文字說明，不要自行臆測與延伸"

    private val dotenv = dotenv {
        directory = "./assets"
        filename = "env"
    }
    private val serverIP = dotenv["SUMMARY_URL"]
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun getSummaryText() {
        val intent = Intent("edu.mui.noti.summary.REQUEST_ALLNOTIS")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun updateSummaryText(postContent: String) {
        _result.postValue("通知摘要產生中，請稍候...")
        viewModelScope.launch {
            Log.d(TAG, postContent)
            sendToServer(postContent)
        }
    }

    private suspend fun sendToServer(content: String) = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val postBody = "{\"prompt\": \"$prompt\", \"content\": \"${content.replace("\n", "\\n")}\"}"
        val request = Request.Builder()
            .url(serverIP)
            .post(postBody.toRequestBody(mediaType))
            .build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                _result.postValue(response.body?.string())
            } else {
                response.body?.let { Log.i("Server", it.string()) }
            }
        }
    }
}