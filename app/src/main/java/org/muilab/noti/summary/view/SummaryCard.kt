package org.muilab.noti.summary.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import org.muilab.noti.summary.viewModel.PromptViewModel
import org.muilab.noti.summary.viewModel.SummaryViewModel

enum class SummaryResponse(val message: String) {
    HINT("請按下方按鈕產生通知摘要"),
    GENERATING("通知摘要產生中，請稍候..."),
    NO_NOTIFICATION("您的手機目前沒有通知"),
    NETWORK_ERROR("無法連線，請確認您的網路設定"),
    SERVER_ERROR("伺服器發生錯誤，請稍後再試"),
    TIMEOUT_ERROR("伺服器忙碌中，請稍後再試"),
    APIKEY_ERROR("請確認您的 API 金鑰是否有誤"),
}

@Composable
fun SummaryCard(sumViewModel: SummaryViewModel, promptViewModel: PromptViewModel, submitButtonState: SSButtonState, setSubmitButtonState: (SSButtonState) -> Unit) {
    val result by sumViewModel.result.observeAsState(SummaryResponse.HINT.message)

    Card(modifier = Modifier.fillMaxSize()) {
        // Credit(context, lifecycleOwner, userId)
        // Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        promptViewModel.promptSentence.value?.let { CurrentPrompt(it) }
        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 16.dp))
        Box(modifier = Modifier
            .padding(16.dp, 4.dp)
            .verticalScroll(rememberScrollState())) {
            Text(text = result)

            if (result == SummaryResponse.GENERATING.message) {
                setSubmitButtonState(SSButtonState.LOADING)
            } else if (result == SummaryResponse.NO_NOTIFICATION.message ||
                       result == SummaryResponse.NETWORK_ERROR.message ||
                       result == SummaryResponse.SERVER_ERROR.message ||
                       result == SummaryResponse.TIMEOUT_ERROR.message ||
                       result == SummaryResponse.APIKEY_ERROR.message
            ) {
                setSubmitButtonState(SSButtonState.FAILIURE)
            } else if (submitButtonState == SSButtonState.LOADING){
                setSubmitButtonState(SSButtonState.SUCCESS)
            }

        }
    }
}

@Composable
fun CurrentPrompt(curPrompt: String) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp, 4.dp)) {
        Text(
            text = curPrompt,
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold,
            ),
        )
    }
}