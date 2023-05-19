package org.muilab.noti.summary.view.userInit

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.muilab.noti.summary.R

@Composable
fun AskPermissionDialog(onAgree: () -> Unit, OpenPermission: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.turn_on_permission)) },
        text = { Text(stringResource(R.string.turnon_permission_description), softWrap = true) },
        confirmButton = { Button(onClick = onAgree) { Text(stringResource(R.string.continue_to_use)) } },
        dismissButton = { Button(onClick = OpenPermission) { Text(stringResource(R.string.enable)) } },
    )
}
