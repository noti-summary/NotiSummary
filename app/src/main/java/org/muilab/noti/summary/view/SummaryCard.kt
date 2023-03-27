package org.muilab.noti.summary.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import org.muilab.noti.summary.database.firestore.FirestoreDocument
import org.muilab.noti.summary.database.firestore.documentStateOf
import org.muilab.noti.summary.maxCredit
import org.muilab.noti.summary.model.UserCredit
import org.muilab.noti.summary.viewModel.PromptViewModel
import org.muilab.noti.summary.viewModel.SummaryViewModel
import kotlin.text.Typography

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