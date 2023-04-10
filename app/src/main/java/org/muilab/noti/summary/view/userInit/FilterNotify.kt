package org.muilab.noti.summary.view.userInit

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.muilab.noti.summary.R

@Composable
fun FilterNotify(onAgree: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.noti_covered_dialog_title)) },
        text = { Text(stringResource(R.string.noti_covered_dialog_content), softWrap = true) },
        confirmButton = { Button(onClick = onAgree) { Text(stringResource(R.string.ok))} },
    )
}
