package org.muilab.noti.summary.view.settings

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun APIKeyScreen() {
    Column(modifier = Modifier.fillMaxHeight()) { TextBoxForSetAPI() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextBoxForSetAPI() {
    // TODO: Store the API key in SharedPreference or others
    var apiKey by remember { mutableStateOf("") }
    var displayApiKey by remember { mutableStateOf("sk-********") }

    fun modifyDisplayApiKey(newApiKey: String) {
        if (!newApiKey.startsWith("sk-")) {
            // TODO: Show users the API key is in the wrong format
            return
        }
        val lastPartOfApiKey = newApiKey.takeLast(4)
        Log.d("modifyDisplayApiKey", lastPartOfApiKey)
        displayApiKey = displayApiKey.dropLast(4) + lastPartOfApiKey
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API 金鑰") },
            placeholder = { Text("輸入您的 API 金鑰") })

        Button(
            onClick = {
                // TODO: Store the API key in SharedPreference or others
                modifyDisplayApiKey(apiKey)
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
