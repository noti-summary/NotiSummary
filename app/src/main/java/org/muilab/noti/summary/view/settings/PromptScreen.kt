package org.muilab.noti.summary.view.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.muilab.noti.summary.R
import org.muilab.noti.summary.view.component.NoPaddingAlertDialog
import org.muilab.noti.summary.viewModel.PromptViewModel

@Composable
fun PromptScreen(context: Context, promptViewModel: PromptViewModel) {
    MaterialTheme {
        PromptHistory(context, promptViewModel = promptViewModel)
        AddButton(promptViewModel = promptViewModel)
    }
}

@Composable
fun PromptHistory(context: Context, promptViewModel: PromptViewModel) {
    val selectedOption = promptViewModel.promptSentence.observeAsState()
    val allPromptSentence = promptViewModel.allPromptSentence.observeAsState(listOf(""))
    val defaultPrompt = "Summarize the notifications in a Traditional Chinese statement."

    val showDialog = remember { mutableStateOf(false) }
    val currentEditPrompt = remember { mutableStateOf("") }
    var currentEditPromptOriginalValue by remember { mutableStateOf("") }

    val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        itemsIndexed(listOf(defaultPrompt) + allPromptSentence.value) { index, item ->
            if (index == 0) {
                Text(
                    stringResource(R.string.default_prompt),
                    modifier = Modifier.padding(start = 15.dp, top = 10.dp, bottom = 10.dp)
                )
            } else if (index == 1) {
                Text(
                    stringResource(R.string.custom_prompt),
                    modifier = Modifier.padding(start = 15.dp, top = 10.dp, bottom = 10.dp)
                )
            }
            Card(
                modifier = Modifier
                    .padding(start = 15.dp, end = 15.dp, top = 2.dp, bottom = 2.dp)
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
                            val clip = ClipData.newPlainText("copy prompt text", item)
                            clipboardManager.setPrimaryClip(clip)
                            Toast.makeText(
                                context,
                                context.getString(R.string.copied_to_clipboard),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.copy),
                            contentDescription = "copy content",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (item != defaultPrompt) {
                        IconButton(
                            onClick = {
                                currentEditPrompt.value = item
                                currentEditPromptOriginalValue = item
                                showDialog.value = true
                            }
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = "edit prompt")
                        }

                        IconButton(
                            onClick = {
                                promptViewModel.deletePrompt(item)
                            }
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = "delete prompt")
                        }
                    }
                }
            }
        }
    }

    val confirmAction = {
        if (currentEditPrompt.value != "") {
            promptViewModel.updatePrompt(
                currentEditPromptOriginalValue,
                currentEditPrompt.value
            )
            currentEditPrompt.value = ""
            showDialog.value = false
        }
    }

    if (showDialog.value) {
        PromptEditor(showDialog, currentEditPrompt, confirmAction)
    }
}

@Composable
fun AddButton(promptViewModel: PromptViewModel) {

    val showDialog = remember { mutableStateOf(false) }
    val inputPrompt = remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 20.dp, end = 20.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { showDialog.value = true },
        ) {
            Icon(Icons.Filled.Add, "add new prompt")
        }
    }

    val confirmAction = {
        if (inputPrompt.value != "") {
            Log.d("currentEditPrompt", inputPrompt.value)
            promptViewModel.addPrompt(inputPrompt.value)
            inputPrompt.value = ""
            showDialog.value = false
        }
    }

    val dismissAction = { inputPrompt.value = "" }

    if (showDialog.value) {
        PromptEditor(showDialog, inputPrompt, confirmAction, dismissAction)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptEditor(
    showDialog: MutableState<Boolean>,
    defaultPromptInTextBox: MutableState<String>,
    confirmAction: () -> Unit,
    dismissAction: () -> Unit = {},
) {
    NoPaddingAlertDialog(
        title = {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, bottom = 20.dp)
                    .height(70.dp),
                painter = painterResource(id = R.drawable.prompt),
                contentDescription = "prompt_icon",
            )
        },
        text = {
            OutlinedTextField(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp).fillMaxWidth(),
                singleLine = true,
                value = defaultPromptInTextBox.value,
                onValueChange = { defaultPromptInTextBox.value = it },
                label = { Text(stringResource(R.string.prompt)) },
            )
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.padding(all = 3.dp),
                onClick = {
                    confirmAction()
                }
            )
            {
                Text(
                    text = stringResource(R.string.ok),
                    modifier = Modifier.padding(start = 30.dp, end = 30.dp)
                )
            }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier.padding(all = 3.dp),
                onClick = {
                    dismissAction()
                    showDialog.value = false
                }
            )
            {
                Text(
                    text = stringResource(R.string.cancel),
                    modifier = Modifier.padding(start = 30.dp, end = 30.dp)
                )
            }
        }
    )
}
