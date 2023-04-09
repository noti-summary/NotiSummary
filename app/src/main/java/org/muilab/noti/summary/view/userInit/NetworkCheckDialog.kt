package org.muilab.noti.summary.view.userInit

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.muilab.noti.summary.MainActivity

@Composable
fun NetworkCheckDialog(context: Context) {

    val activity = (LocalContext.current as? Activity)

    AlertDialog(
        onDismissRequest = {},
        title = { Text("請確認網路連線") },
        confirmButton = {
            Button(onClick = {
                val restartIntent = Intent(context, MainActivity::class.java)
                restartIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(restartIntent)
                activity?.finish()
            }) {
                Text("我已連上網際網路")
            }
        },
    )
}
