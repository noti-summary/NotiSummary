package org.muilab.noti.summary.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.muilab.noti.summary.R
import org.muilab.noti.summary.view.component.NoPaddingAlertDialog
import org.muilab.noti.summary.viewModel.APIKeyViewModel

@Composable
fun APIKeyScreen(apiKeyViewModel: APIKeyViewModel) {

    MaterialTheme {
        APIKeyList(apiKeyViewModel)
        AddKeyButton(apiKeyViewModel)
    }

}


@Composable
fun APIKeyList(apiKeyViewModel: APIKeyViewModel) {
    val selectedOption = apiKeyViewModel.apiKey.observeAsState()
    val allAPIKey = apiKeyViewModel.allAPIKey.observeAsState(listOf(""))
    val defaultAPIKey = "預設 API Key"

    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        itemsIndexed(listOf(defaultAPIKey) + allAPIKey.value) { index, item ->
            if (index == 0) {
                Text("預設 API Key", modifier = Modifier.padding(start = 15.dp, top = 10.dp, bottom = 10.dp))
            } else if (index == 1) {
                Text("自訂 API Key", modifier = Modifier.padding(start = 15.dp, top = 10.dp, bottom = 10.dp))
            }
            Card(
                modifier = Modifier
                    .padding(start = 15.dp, end = 15.dp, top = 2.dp, bottom = 2.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable {
                        apiKeyViewModel.chooseAPI(item)
                    },
                colors = CardDefaults.cardColors(
                    containerColor =
                    if (item == selectedOption.value) {
                        Color.DarkGray
                    } else {
                        Color.Gray
                    }
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = if (item != defaultAPIKey) {
                            "sk-**********" + item.takeLast(4)
                        } else {
                            defaultAPIKey
                        },
                    )

                    if (item != defaultAPIKey) {
                        IconButton(onClick = { apiKeyViewModel.deleteAPI(item) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "delete api")
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun AddKeyButton(apiKeyViewModel: APIKeyViewModel) {

    val showDialog = remember { mutableStateOf(false) }
    val inputKey = remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 20.dp, end = 20.dp),
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
            apiKeyViewModel.addAPI(inputKey.value)
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
