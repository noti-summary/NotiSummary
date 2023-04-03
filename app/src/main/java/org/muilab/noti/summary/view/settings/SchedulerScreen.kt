package org.muilab.noti.summary.view.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.muilab.noti.summary.receiver.AlarmReceiver
import org.muilab.noti.summary.util.addAlarm
import org.muilab.noti.summary.viewModel.ScheduleViewModel
import java.util.*

@Composable
fun SchedulerScreen(context: Context, scheduleViewModel: ScheduleViewModel) {
    TimeList(context, scheduleViewModel)
    AddScheduleButton(context, scheduleViewModel)
}

@Composable
fun TimeList(context: Context, scheduleViewModel: ScheduleViewModel) {
    val allSchedule = scheduleViewModel.allSchedule.observeAsState(listOf(""))
    lateinit var oldTime: String

    val listener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
        Log.d("TimeList", "oldTime: $oldTime")
        Log.d("TimeList", "newTime: $hour:$minute")
        scheduleViewModel.updateSchedule(oldTime, "$hour:$minute")
    }

    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        itemsIndexed(allSchedule.value) { index, item ->
            if (index == 0) {
                Text("排程", modifier = Modifier.padding(start = 15.dp, top = 10.dp, bottom = 10.dp))
            }
            Card(
                modifier = Modifier
                    .padding(start = 15.dp, end = 15.dp, top = 2.dp, bottom = 2.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(containerColor = Color.Gray),
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = item,
                    )
                    IconButton(onClick = {
                        val hour: Int = item.split(':')[0].toInt()
                        val minute: Int = item.split(':')[1].toInt()
                        oldTime = item
                        val picker = TimePickerDialog(context, listener, hour, minute, false)
                        picker.show()
                    }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "edit schedule")
                    }

                    IconButton(onClick = { scheduleViewModel.deleteSchedule(item) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "delete schedule")
                    }
                }
            }
        }
    }
}

@Composable
fun AddScheduleButton(context: Context, scheduleViewModel: ScheduleViewModel) {
    val listener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
        scheduleViewModel.addNewSchedule("$hour:$minute")
        addAlarm(context, hour, minute)
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 20.dp, end = 20.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(onClick = {
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
}