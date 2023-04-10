package org.muilab.noti.summary.view.home

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import org.muilab.noti.summary.R
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
fun SummaryCard(context: Context, sumViewModel: SummaryViewModel, promptViewModel: PromptViewModel, submitButtonState: SSButtonState, setSubmitButtonState: (SSButtonState) -> Unit) {
    val result by sumViewModel.result.observeAsState(SummaryResponse.HINT.message)

    Card(modifier = Modifier.fillMaxSize()) {
        promptViewModel.promptSentence.value?.let { CurrentPrompt(it) }

        val summaryPerfs = context.getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)
        val likeDislike  = remember { mutableStateOf(summaryPerfs.getInt("rating", 0)) }

        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Box(modifier = Modifier
            .padding(16.dp, 4.dp)
            .verticalScroll(rememberScrollState())
        ) {

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    DislikeButton(likeDislike, summaryPerfs)
                    LikeButton(likeDislike, summaryPerfs)
                }

                Text(text = result)
            }

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

                summaryPerfs.edit().putInt("rating", 0).apply()
                likeDislike.value = 0
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
            text = "> $curPrompt",
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold,
            ),
        )
    }
}

@Composable
fun LikeButton(likeDislike: MutableState<Int>, summaryPerfs: SharedPreferences) {
    IconButton(
        onClick = {
            if (likeDislike.value == 1) {
                likeDislike.value = 0
            } else {
                likeDislike.value = 1
            }
            summaryPerfs.edit().putInt("rating", likeDislike.value).apply()
        }
    ) {
        Icon(
            painter  = painterResource(R.drawable.thumb_up_500),
            contentDescription = "Like",
            tint = if (likeDislike.value == 1) Color.Cyan else Color.Gray
        )
    }
}

@Composable
fun DislikeButton(likeDislike: MutableState<Int>, summaryPerfs: SharedPreferences) {
    IconButton(
        onClick = {
            if (likeDislike.value == -1) {
                likeDislike.value = 0
            } else {
                likeDislike.value = -1
            }
            summaryPerfs.edit().putInt("rating", likeDislike.value).apply()
        }
    ) {
        Icon(
            painter  = painterResource(R.drawable.thumb_down_500),
            contentDescription = "Dislike",
            tint = if (likeDislike.value == -1) Color.Red else Color.Gray
        )
    }
}