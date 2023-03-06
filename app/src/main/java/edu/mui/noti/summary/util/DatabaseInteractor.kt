import android.content.Context
import android.util.Log
import edu.mui.noti.summary.database.room.CurrentDrawerDatabase
import edu.mui.noti.summary.util.TAG
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

fun SendCurrentDrawerToServer(context: Context) {
    val dotenv = dotenv {
        directory = "./assets"
        filename = "env"
    }
    val serverIP = dotenv["SUMMARY_URL"]

    val currentDrawerDao = CurrentDrawerDatabase.getInstance(context).currentDrawerDao()
    GlobalScope.launch {
        val tmpreq = currentDrawerDao.getAll().toString()
        Log.d(TAG, tmpreq)
        // TODO: replace postBody with the current drawer
        val postBody =
            "{\"prompt\": \"You are a helpful assistant.\", \"content\": \"How are you?\"}"

        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(serverIP)
            .post(postBody.toRequestBody(mediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("http_request", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                Log.d("SendCurrentDrawerToServer", res.toString())
            }
        })
    }
}
