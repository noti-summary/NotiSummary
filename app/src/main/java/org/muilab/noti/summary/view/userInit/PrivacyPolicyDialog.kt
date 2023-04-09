package org.muilab.noti.summary.view.userInit

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun PrivacyPolicyDialog(onAgree: () -> Unit) {
    var agree by remember { mutableStateOf(false) }
    val privacyURL = "https://www.example.com" // TODO: Change in the future
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val privacyHeight = screenHeight * 3 / 5

    AlertDialog(
        onDismissRequest = {},
        modifier = Modifier.wrapContentSize(),
        title = { Text(text = "隱私權政策") },
        text = {
            Column {
                Box {
                    AndroidView(
                        factory = {
                            WebView(it).apply {
                                loadUrl(privacyURL)
                            }
                        },
                        modifier = Modifier.height(privacyHeight)
                    )
                }
                Row (verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = agree,
                        onCheckedChange = { agree = it }
                    )
                    Text(text = "我同意本APP之隱私權政策")
                }
            }
        },
        confirmButton = {
            Button(onClick = onAgree, enabled = agree) {
                Text(text = "Agree")
            }
        }
    )
}
