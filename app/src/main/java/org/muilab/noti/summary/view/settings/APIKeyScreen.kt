package org.muilab.noti.summary.view.settings

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.dp
import org.muilab.noti.summary.R
import org.muilab.noti.summary.util.getActiveNotifications
import org.muilab.noti.summary.view.component.NoPaddingAlertDialog

@Composable
fun APIKeyScreen(context: Context) {

    val sharedPref = context.getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)

    val displayApiKey = remember {
        val apiKeyInSharedPref =  sharedPref.getString("userAPIKey", "")
        if (apiKeyInSharedPref == "")
            mutableStateOf("sk-********")
        else
            mutableStateOf("sk-****" + apiKeyInSharedPref!!.takeLast(4))
    }

    MaterialTheme {
//        Column(modifier = Modifier.fillMaxHeight()) { TextBoxForSetAPI(context, displayApiKey) }
        Text(displayApiKey.value)
        AddKeyButton(sharedPref, displayApiKey)
    }

}


@Composable
fun APIKeyList() {

}


@Composable
fun AddKeyButton(sharedPref: SharedPreferences, displayApiKey: MutableState<String>,) {

    val showDialog = remember { mutableStateOf(false) }
    val inputKey = remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 120.dp, end = 120.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { showDialog.value = true },
        ) {
            Icon(Icons.Filled.Add, "add new key")
        }
    }

    val confirmAction = {
        if (inputKey.value != "") {

            with(sharedPref.edit()) {
                putString("userAPIKey", inputKey.value)
                apply()
            }

            val lastPartOfApiKey = inputKey.value.takeLast(4)
            displayApiKey.value = displayApiKey.value.dropLast(4) + lastPartOfApiKey

            inputKey.value = ""
            showDialog.value = false
        }
    }

    val dismissAction = { inputKey.value = "" }

    if (showDialog.value) {
        APIKeyEditor(showDialog, inputKey, confirmAction, dismissAction)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun APIKeyEditor(
    showDialog: MutableState<Boolean>,
    defaultPromptInTextBox: MutableState<String>,
    confirmAction: () -> Unit,
    dismissAction: () -> Unit = {},
) {
    NoPaddingAlertDialog(
        title = {
            Image(
                modifier = Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 20.dp).height(70.dp),
                painter = painterResource(id = R.drawable.key),
                contentDescription = "key_icon",
            )
        },
        text = {
            OutlinedTextField(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp).fillMaxWidth(),
                singleLine = true,
                value = defaultPromptInTextBox.value,
                onValueChange = { defaultPromptInTextBox.value = it },
                label = { Text("API 金鑰") },
            )
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.padding(all = 3.dp),
                onClick = {
                    confirmAction()
                }
            )
            { Text(text = "確認", modifier = Modifier.padding(start = 30.dp, end = 30.dp)) }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier.padding(all = 3.dp),
                onClick = {
                    dismissAction()
                    showDialog.value = false
                }
            )
            { Text(text = "取消", modifier = Modifier.padding(start = 30.dp, end = 30.dp)) }
        }
    )
}


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TextBoxForSetAPI(context: Context, displayApiKey: MutableState<String>) {
//    val sharedPref = context.getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)
//    var apiKey: String by remember { mutableStateOf("") }
//
//    fun updateApiKey(newApiKey: String) {
//        if (!newApiKey.startsWith("sk-")) {
//            // TODO: Show users the API key is in the wrong format
//            return
//        }
//
//        with(sharedPref.edit()) {
//            putString("userAPIKey", newApiKey)
//            apply()
//        }
//
//        val lastPartOfApiKey = newApiKey.takeLast(4)
//        displayApiKey.value = displayApiKey.value.dropLast(4) + lastPartOfApiKey
//    }
//
//    Row(modifier = Modifier.fillMaxWidth()) {
//        TextField(
//            value = apiKey,
//            onValueChange = { apiKey = it },
//            label = { Text("API 金鑰") },
//            placeholder = { Text("輸入您的 API 金鑰") },
//            modifier = Modifier.fillMaxWidth(0.7F),
//        )
//
//        Button(
//            onClick = {
//                updateApiKey(apiKey)
//                apiKey = ""
//            }) {
//              Text("更新")
//            }
//        }
//    Row(modifier = Modifier.fillMaxWidth()) {
//          if (displayApiKey.value == "sk-********") {
//              Text("OpenAI 金鑰格式：")
//          } else {
//              Text("您的 OpenAI 金鑰：")
//          }
//          Text(displayApiKey.value)
//    }
//
//}
