package org.muilab.noti.summary.view.settings

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.muilab.noti.summary.R
import org.muilab.noti.summary.viewModel.PromptViewModel

@Composable
fun PromptScreen(promptViewModel: PromptViewModel) {
    MaterialTheme {
        PromptHistory(promptViewModel = promptViewModel)
        AddButton(promptViewModel = promptViewModel)
    }
}


@Composable
fun PromptHistory(promptViewModel: PromptViewModel) {
    val selectedOption = promptViewModel.promptSentence.observeAsState()
    val allPromptSentence = promptViewModel.allPromptSentence.observeAsState(listOf(""))
    val defaultPrompt = "Summarize the notifications in a Traditional Chinese statement."

    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        itemsIndexed(listOf(defaultPrompt) + allPromptSentence.value) { index, item ->
            if (index == 0) {
                Text("預設摘要提示句", modifier = Modifier.padding(all = 8.dp))
            } else if (index == 1) {
                Text("自訂摘要提示句", modifier = Modifier.padding(all = 8.dp))
            }
            Card(
                modifier = Modifier
                    .padding(3.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable {
                        promptViewModel.choosePrompt(item)
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
                        text = item,
                    )

                    IconButton(
                        onClick = {
                            // Perform edit action
                        }
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = "edit prompt")
                    }

                    IconButton(
                        onClick = {
                            // Perform delete action
                        }
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = "delete prompt")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddButton(promptViewModel: PromptViewModel) {

    var showDialog by remember { mutableStateOf(false) }
    var inputPrompt by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 100.dp, end = 25.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { showDialog = true },
        ) {
            Icon(Icons.Filled.Add, "add new prompt")
        }
    }

    if (showDialog) {
        NoPaddingAlertDialog(
            onDismissRequest = { },
            title = {
                Image(
                    modifier = Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 20.dp).height(70.dp),
                    painter = painterResource(id = R.drawable.prompt),
                    contentDescription = "prompt_icon",
                )
            },
            text = {
                OutlinedTextField(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp).fillMaxWidth(),
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    label = { Text("提示句") },
                )
            },
            confirmButton = {
                TextButton(
                    modifier = Modifier.padding(all = 3.dp),
                    onClick = {
                        if (inputPrompt != "") {
                            promptViewModel.addPrompt(inputPrompt)
                            inputPrompt = ""
                            showDialog = false
                        }
                    }
                )
                { Text(text = "確認", modifier = Modifier.padding(start = 30.dp, end = 30.dp)) }
            },
            dismissButton = {
                TextButton(
                    modifier = Modifier.padding(all = 3.dp),
                    onClick = {
                        showDialog = false
                    }
                )
                { Text(text = "取消", modifier = Modifier.padding(start = 30.dp, end = 30.dp)) }
            }
        )
    }

}


@Composable
fun NoPaddingAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    properties: DialogProperties = DialogProperties()
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier,
            shape = shape,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                title?.let {
                    CompositionLocalProvider() {
                        val textStyle = MaterialTheme.typography.titleLarge
                        ProvideTextStyle(textStyle, it)
                    }
                }
                text?.let {
                    CompositionLocalProvider() {
                        val textStyle = MaterialTheme.typography.labelSmall
                        ProvideTextStyle(textStyle, it)
                    }
                }
                Box(
                    Modifier.fillMaxWidth().padding(all = 6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        dismissButton?.invoke()
                        confirmButton()
                    }
                }
            }
        }
    }
}