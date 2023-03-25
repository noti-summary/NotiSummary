package org.muilab.noti.summary.view.settings

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.muilab.noti.summary.util.getActiveNotifications

@Composable
fun APIKeyScreen(context: Context) {
    Column(modifier = Modifier.fillMaxHeight()) { TextBoxForSetAPI(context) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextBoxForSetAPI(context: Context) {
    val sharedPref = context.getSharedPreferences("userAPIKey", Context.MODE_PRIVATE)
    var apiKey: String by remember { mutableStateOf("") }
    var displayApiKey by remember {
        val apiKeyInSharedPref =  sharedPref.getString("userAPIKey", "")
        if (apiKeyInSharedPref == "")
            mutableStateOf("sk-********")
        else
            mutableStateOf("sk-****" + apiKeyInSharedPref!!.takeLast(4))
    }

    fun updateApiKey(newApiKey: String) {
        if (!newApiKey.startsWith("sk-")) {
            // TODO: Show users the API key is in the wrong format
            return
        }

        with(sharedPref.edit()) {
            putString("userAPIKey", newApiKey)
            apply()
        }

        val lastPartOfApiKey = newApiKey.takeLast(4)
        displayApiKey = displayApiKey.dropLast(4) + lastPartOfApiKey
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API 金鑰") },
            placeholder = { Text("輸入您的 API 金鑰") },
            modifier = Modifier.fillMaxWidth(0.7F),
        )

        Button(
            onClick = {
                updateApiKey(apiKey)
                apiKey = ""
            }) {
              Text("更新")
            }
        }
    Row(modifier = Modifier.fillMaxWidth()) {
          if (displayApiKey == "sk-********") {
              Text("OpenAI 金鑰格式：")
          } else {
              Text("您的 OpenAI 金鑰：")
          }
          Text(displayApiKey)
    }

}
