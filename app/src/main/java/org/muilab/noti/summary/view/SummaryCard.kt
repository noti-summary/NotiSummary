package org.muilab.noti.summary.view

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
import androidx.compose.ui.text.font.FontWeight
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
    TIMEOUT_ERROR("伺服器忙碌中，請稍後再試")
}

@Composable
fun SummaryCard(userId:String, lifecycleOwner: LifecycleOwner, sumViewModel: SummaryViewModel, promptViewModel: PromptViewModel, submitButtonState: SSButtonState, setSubmitButtonState: (SSButtonState) -> Unit) {
    val result by sumViewModel.result.observeAsState(SummaryResponse.HINT.message)

    Card(modifier = Modifier.fillMaxSize()) {
        promptViewModel.promptSentence.value?.let { CurrentPrompt(it) }
        Credit(lifecycleOwner, userId)
        Box(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(text = result)

            if (result == SummaryResponse.GENERATING.message) {
                setSubmitButtonState(SSButtonState.LOADING)
            } else if (result == SummaryResponse.NO_NOTIFICATION.message || result == SummaryResponse.NETWORK_ERROR.message || result == SummaryResponse.SERVER_ERROR.message || result == SummaryResponse.TIMEOUT_ERROR.message) {
                setSubmitButtonState(SSButtonState.FAILIURE)
            } else if (submitButtonState == SSButtonState.LOADING){
                setSubmitButtonState(SSButtonState.SUCCESS)
            }

        }
    }
}

@Composable
fun Credit(lifecycleOwner: LifecycleOwner, userId: String) {

    val documentRef = Firebase.firestore.collection("user-free-credit").document(userId)
    val (result) = remember { documentStateOf(documentRef, lifecycleOwner) }
    var displayText by remember { mutableStateOf("今日可再進行 - 次摘要") }

    if (result is FirestoreDocument.Snapshot) {
        if (result.snapshot.exists()) {
            val res = result.snapshot.toObject<UserCredit>()!!
            displayText = "今日可再進行 ${res.credit} 次摘要"
        } else {
            displayText = "${SummaryResponse.NETWORK_ERROR.message}，並重新啟動 app"
        }
    }

    Card(modifier = Modifier
        .fillMaxWidth().padding(16.dp, 4.dp)) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                text = displayText,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }
    }

}

@Composable
fun CurrentPrompt(curPrompt: String) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp, 0.dp)) {
        Box (modifier = Modifier.background(MaterialTheme.colorScheme.secondary)) {
            Text(
                text = "當前摘要提示句：$curPrompt",
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }
    }
}