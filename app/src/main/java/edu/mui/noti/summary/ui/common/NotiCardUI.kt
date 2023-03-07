package edu.mui.noti.summary.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.mui.noti.summary.viewModel.SummaryViewModel

@Composable
fun NotiCard(sumViewModel: SummaryViewModel) {
    val result by sumViewModel.result.observeAsState("The Summary of notification is preparing...")
    Column(modifier = Modifier.padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth().height(500.dp)) {
            Box(modifier = Modifier.padding(16.dp)) {
                Text(text = result)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            sumViewModel.updateSummaryText()
        }) {
            Text(text = "Send to Server")
        }
    }
}
