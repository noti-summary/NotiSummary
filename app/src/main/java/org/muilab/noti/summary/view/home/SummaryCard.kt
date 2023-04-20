package org.muilab.noti.summary.view.home

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import org.muilab.noti.summary.R
import org.muilab.noti.summary.util.logSummary
import org.muilab.noti.summary.viewModel.PromptViewModel
import org.muilab.noti.summary.viewModel.SummaryViewModel

enum class SummaryResponse(val message: Int) {
    HINT(R.string.hint_msg),
    GENERATING(R.string.gen_msg),
    NO_NOTIFICATION(R.string.no_noti_msg),
    NETWORK_ERROR(R.string.network_err_msg),
    SERVER_ERROR(R.string.server_err_msg),
    TIMEOUT_ERROR(R.string.timeout_msg),
    APIKEY_ERROR(R.string.key_msg),
    QUOTA_ERROR(R.string.quota_msg)
}

@Composable
fun SummaryCard(
    context: Context,
    sumViewModel: SummaryViewModel,
    promptViewModel: PromptViewModel,
    submitButtonState: SSButtonState,
    setSubmitButtonState: (SSButtonState) -> Unit
) {
    val result by sumViewModel.result.observeAsState(stringResource(SummaryResponse.HINT.message))

    Card(modifier = Modifier.fillMaxSize()) {
        promptViewModel.promptSentence.value?.let { CurrentPrompt(it) }

        val summaryPrefs = context.getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)
        val likeDislike  = remember { mutableStateOf(summaryPrefs.getInt("rating", 0)) }

        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Box(modifier = Modifier.padding(16.dp, 4.dp)) {
            Column {
                Column(modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = result,
                        style = TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            letterSpacing = 0.sp
                        ),
                    )
                }
                Box(
                    modifier = Modifier.height(40.dp).fillMaxWidth(),
                    contentAlignment = Alignment.BottomEnd // align Row to the bottom end
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        DislikeButton(context, likeDislike, summaryPrefs)
                        LikeButton(context, likeDislike, summaryPrefs)
                    }
                }
            }

            if (result == stringResource(SummaryResponse.GENERATING.message)) {
                setSubmitButtonState(SSButtonState.LOADING)
            } else if (result == stringResource(SummaryResponse.NO_NOTIFICATION.message) ||
                       result == stringResource(SummaryResponse.NETWORK_ERROR.message) ||
                       result == stringResource(SummaryResponse.SERVER_ERROR.message) ||
                       result == stringResource(SummaryResponse.TIMEOUT_ERROR.message) ||
                       result == stringResource(SummaryResponse.APIKEY_ERROR.message) ||
                       result == stringResource(SummaryResponse.QUOTA_ERROR.message)
            ) {
                setSubmitButtonState(SSButtonState.FAILIURE)
            } else if (submitButtonState == SSButtonState.LOADING){
                setSubmitButtonState(SSButtonState.SUCCESS)

                summaryPrefs.edit().putInt("rating", 0).apply()
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
fun LikeButton(
    context: Context,
    likeDislike: MutableState<Int>,
    summaryPrefs: SharedPreferences
) {
    IconButton(
        onClick = {
            if (likeDislike.value == 1) {
                likeDislike.value = 0
            } else {
                likeDislike.value = 1
            }
            summaryPrefs.edit().putInt("rating", likeDislike.value).apply()
            logSummary(context)
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
fun DislikeButton(
    context: Context,
    likeDislike: MutableState<Int>,
    summaryPrefs: SharedPreferences
) {
    IconButton(
        onClick = {
            if (likeDislike.value == -1) {
                likeDislike.value = 0
            } else {
                likeDislike.value = -1
            }
            summaryPrefs.edit().putInt("rating", likeDislike.value).apply()
            logSummary(context)
        }
    ) {
        Icon(
            painter  = painterResource(R.drawable.thumb_down_500),
            contentDescription = "Dislike",
            tint = if (likeDislike.value == -1) Color.Red else Color.Gray
        )
    }
}