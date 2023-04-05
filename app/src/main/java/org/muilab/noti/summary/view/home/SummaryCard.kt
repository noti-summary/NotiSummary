package org.muilab.noti.summary.view.home

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import org.muilab.noti.summary.R
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
    sumViewModel: SummaryViewModel,
    promptViewModel: PromptViewModel,
    submitButtonState: SSButtonState,
    setSubmitButtonState: (SSButtonState) -> Unit
) {
    val result by sumViewModel.result.observeAsState(stringResource(SummaryResponse.HINT.message))

    Card(modifier = Modifier.fillMaxSize()) {
        promptViewModel.promptSentence.value?.let { CurrentPrompt(it) }
        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 16.dp))
        Box(modifier = Modifier
            .padding(16.dp, 4.dp)
            .verticalScroll(rememberScrollState())) {
            Text(text = result)

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