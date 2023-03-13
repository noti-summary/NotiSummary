package org.muilab.noti.summary.view

import android.icu.text.CaseMap.Title
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.muilab.noti.summary.R

@Composable
fun SettingsScreen() {
    val settingList = arrayListOf<String>(
        "A",
        "B",
        "C",
    )
    Column() {
        Text(
            text = "設定",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(settingList) {
                Card(
                    modifier = Modifier
                        .padding(3.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(), text=it)
                }
            }
        }
    }
}