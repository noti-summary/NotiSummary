package org.muilab.noti.summary.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.muilab.noti.summary.view.SettingScreenItem

@Composable
fun MainSettingScreen(navController: NavHostController) {
    Column() {
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(
                items = SettingScreenItem.values()
                    .slice(1 until SettingScreenItem.values().size)
            ) {
                Card(
                    modifier = Modifier
                        .padding(3.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clickable { navController.navigate(it.name) },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        text = it.title
                    )
                }
            }
        }
    }
}