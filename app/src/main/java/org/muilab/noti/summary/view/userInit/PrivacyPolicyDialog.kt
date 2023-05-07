package org.muilab.noti.summary.view.userInit

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import org.muilab.noti.summary.R

class CustomWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        view?.loadUrl(request?.url.toString())
        return true
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PrivacyPolicyDialog(onAgree: () -> Unit) {
    var agree by remember { mutableStateOf(false) }
    val privacyURL = stringResource(R.string.privacy_URL) // TODO: Change in the future
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val privacyWidth = screenWidth * 4 / 5
    val privacyHeight = screenHeight * 3 / 5

    AlertDialog(
        onDismissRequest = {},
        modifier = Modifier.wrapContentSize(),
        title = { Text(stringResource(R.string.privacy_policy)) },
        text = {
            Column {
                Box {
                    AndroidView(
                        factory = {
                            WebView(it).apply {
                                webViewClient = CustomWebViewClient()
                                settings.javaScriptEnabled = true
                                loadUrl(privacyURL)
                            }
                        },
                        modifier = Modifier.size(privacyWidth, privacyHeight)
                    )
                }
                Row (verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(privacyWidth)) {
                    Checkbox(
                        checked = agree,
                        onCheckedChange = { agree = it }
                    )
                    Text(stringResource(R.string.agree_privacy_policy))
                }
            }
        },
        confirmButton = {
            Button(onClick = onAgree, enabled = agree) {
                Text(stringResource(R.string.ok))
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    )
}
