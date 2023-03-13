package org.muilab.noti.summary.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import org.muilab.noti.summary.viewModel.SummaryViewModel

@Composable
fun SummaryCard(sumViewModel: SummaryViewModel) {
    val result by sumViewModel.result.observeAsState("請按下方按鈕產生通知摘要")

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val HEIGHT_RATIO = 0.6f
    val cardHeight = (screenHeight * HEIGHT_RATIO).toInt()

    Column(modifier = Modifier.padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth().height(cardHeight.dp)) {
            Box(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text(text = result)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
