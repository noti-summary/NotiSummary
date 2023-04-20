package org.muilab.noti.summary.view.settings

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.muilab.noti.summary.R
import org.muilab.noti.summary.util.addAlarm
import org.muilab.noti.summary.util.deleteAlarm
import org.muilab.noti.summary.util.insertUserAction
import org.muilab.noti.summary.viewModel.ScheduleViewModel
import java.util.*

@Composable
fun SchedulerScreen(context: Context, scheduleViewModel: ScheduleViewModel) {
    TimeList(context, scheduleViewModel)
    AddScheduleButton(context, scheduleViewModel)
}

@Composable
fun TimeList(context: Context, scheduleViewModel: ScheduleViewModel) {
    val allSchedule = scheduleViewModel.allSchedule.observeAsState(listOf())

    Column {
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            itemsIndexed(allSchedule.value) { index, item ->
                if (index == 0) Divider(color = Color.DarkGray, thickness = 1.dp) // TODO: use color in material3 instead?
                Box(
                    modifier = Modifier
                        .padding(start = 15.dp, end = 15.dp, top = 2.dp, bottom = 2.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = item.getTime(),
                            style = MaterialTheme.typography.displaySmall
                        )
                        IconButton(onClick = {
                            scheduleViewModel.deleteSchedule(item)
                            deleteAlarm(context, item)
                        }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "delete schedule")
                        }
                    }
                }
                Divider(color = Color.DarkGray, thickness = 1.dp) // TODO: use color in material3 instead?
            }
        }
    }
}

@Composable
fun AddScheduleButton(context: Context, scheduleViewModel: ScheduleViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    val listener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            val newSchedule = scheduleViewModel.addNewSchedule(hour, minute)
            if (newSchedule != null) {
                addAlarm(context, newSchedule)
                insertUserAction("schedulerDialog", "confirm", context)
            }
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            // Notifications are not enabled, navigate to settings
            showDialog = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 20.dp, end = 20.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(onClick = {
            insertUserAction("schedulerDialog", "launch", context)
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val picker = TimePickerDialog(context, listener, 0, 0, false)
            picker.show()
            picker.updateTime(hour, minute)
        }) {
            Icon(Icons.Filled.Add, "schedule summary")
        }
    }

    if (showDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.no_noti_permission)) },
            text = { Text(stringResource(R.string.click_to_open_permission)) },
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showDialog = false
                        val intent = Intent().apply {
                            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.ok),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 30.dp, end = 30.dp)
                    )
                }
            },
        )
    }
}