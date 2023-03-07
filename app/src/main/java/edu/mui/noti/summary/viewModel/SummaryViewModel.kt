package edu.mui.noti.summary.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import edu.mui.noti.summary.util.TAG
import edu.mui.noti.summary.util.getActiveNotifications
import kotlinx.coroutines.launch
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SummaryViewModel(application: Application): AndroidViewModel(application) {
    private val _result = MutableLiveData<String>()
    val result: LiveData<String> = _result
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private val prompt = "You are a helpful assistant."

    private val dotenv = dotenv {
        directory = "./assets"
        filename = "env"
    }
    private val serverIP = dotenv["SUMMARY_URL"]
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun updateSummaryText() {
        viewModelScope.launch {
            val postBody = getActiveNotifications(context)
            Log.d(TAG, postBody)
            sendToServer(postBody)
        }
    }

    private suspend fun sendToServer(content: String) = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val postBody = "{\"prompt\": \"$prompt\", \"content\": \"$content\"}"
        val request = Request.Builder()
            .url(serverIP)
            .post(postBody.toRequestBody(mediaType))
            .build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                _result.postValue(response.body?.string())
            }
        }
    }
}