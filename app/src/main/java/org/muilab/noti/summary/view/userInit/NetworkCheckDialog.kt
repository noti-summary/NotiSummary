package org.muilab.noti.summary.view.userInit

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.muilab.noti.summary.MainActivity
import org.muilab.noti.summary.R

@Composable
fun NetworkCheckDialog(context: Context) {

    val activity = (LocalContext.current as? Activity)

    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.network_check)) },
        confirmButton = {
            Button(onClick = {
                val restartIntent = Intent(context, MainActivity::class.java)
                restartIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(restartIntent)
                activity?.finish()
            }) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}
