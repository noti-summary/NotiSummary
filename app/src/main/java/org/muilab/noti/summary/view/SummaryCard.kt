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
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import org.muilab.noti.summary.viewModel.SummaryViewModel

enum class SummaryResponse(val message: String) {
    HINT("請按下方按鈕產生通知摘要"),
    GENERATING("通知摘要產生中，請稍候..."),
    NETWORK_ERROR("無法連線，請確認您的網路設定"),
    SERVER_ERROR("伺服器發生錯誤，請稍後再試"),
    TIME_OUT_ERROR("伺服器忙碌中，請稍後再試")
}

@Composable
fun SummaryCard(sumViewModel: SummaryViewModel, submitButtonState: SSButtonState, setSubmitButtonState: (SSButtonState) -> Unit) {
    val result by sumViewModel.result.observeAsState(SummaryResponse.HINT.message)

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val HEIGHT_RATIO = 0.3f
    val cardHeight = (screenHeight * HEIGHT_RATIO).toInt()

    Column(modifier = Modifier.padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth().height(cardHeight.dp)) {
            Box(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {

                Text(text = result)

                if (result == SummaryResponse.GENERATING.message) {
                    setSubmitButtonState(SSButtonState.LOADING)
                } else if (result == SummaryResponse.NETWORK_ERROR.message || result == SummaryResponse.SERVER_ERROR.message || result == SummaryResponse.TIME_OUT_ERROR.message) {
                    setSubmitButtonState(SSButtonState.FAILIURE)
                } else if (submitButtonState == SSButtonState.LOADING){
                    setSubmitButtonState(SSButtonState.SUCCESS)
                }

            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
