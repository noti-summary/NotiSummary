package org.muilab.noti.summary.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.muilab.noti.summary.viewModel.PromptViewModel

@Composable
fun PromptScreen(promptViewModel: PromptViewModel) {
    Column(modifier = Modifier.fillMaxHeight()) {
        PromptEditor(promptViewModel = promptViewModel)
        PromptHistory(promptViewModel = promptViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptEditor(promptViewModel: PromptViewModel) {
    var text by remember { mutableStateOf("") }
    Text("輸入摘要提示句", style = MaterialTheme.typography.bodyLarge)
    Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            colors = TextFieldDefaults.textFieldColors()
        )
        Button(
            onClick = {
                // update PromptViewModel
                promptViewModel.addPrompt(text)
                text = ""
            }) {
            Text("更新")
        }
    }
}

@Composable
fun PromptHistory(promptViewModel: PromptViewModel) {
    val selectedOption = promptViewModel.promptSentence.observeAsState()

    val allPromptSentence = promptViewModel.allPromptSentence.observeAsState(listOf(""))
    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        items(
            items = allPromptSentence.value
        ) {
            Card(
                modifier = Modifier
                    .padding(3.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable {
                        promptViewModel.choosePrompt(it)
                    },
                colors = CardDefaults.cardColors(
                    containerColor =
                    if (it == selectedOption.value) {
                        Color.DarkGray
                    } else {
                        Color.Gray
                    }
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    text = it,
                )
            }
        }
    }
}
