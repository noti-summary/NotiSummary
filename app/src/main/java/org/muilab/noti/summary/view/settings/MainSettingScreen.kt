package org.muilab.noti.summary.view.settings

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.muilab.noti.summary.R
import org.muilab.noti.summary.util.logUserAction


@Composable
fun MainSettingScreen(context: Context, navController: NavHostController) {

    val uriHandler = LocalUriHandler.current

    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    val country = sharedPref.getString("country", "Unknown")
    val countryCode = country!!.substring(5, 7)

    MaterialTheme {
        Column {
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                itemsIndexed(
                    items = SettingScreenItem.values()
                        .slice(1 until SettingScreenItem.values().size)
                ) { idx, item ->
                    if (!(countryCode != "TW" && item.name == SettingScreenItem.Recruitment.name))
                        Card(
                            modifier = Modifier
                                .padding(
                                    start = 10.dp,
                                    end = 10.dp,
                                    top = if (idx == 0 || idx == 5) 6.dp else 1.dp,
                                    bottom = if (idx == 4 || idx == 8) 6.dp else 1.dp
                                )
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clip(
                                    RoundedCornerShape(
                                        topStart = if (idx  == 0 || idx == 5) 12.dp else 0.dp,
                                        topEnd = if (idx  == 0 || idx == 5) 12.dp else 0.dp,
                                        bottomStart = if (idx  == 4 || idx == 8) 12.dp else 0.dp,
                                        bottomEnd = if (idx  == 4 || idx == 8) 12.dp else 0.dp
                                    )
                                )
                                .clickable {
                                    if (item.name == SettingScreenItem.Feedback.name) {
                                        uriHandler.openUri("https://forms.gle/LSe1fZP2sDnXUtC59")
                                    } else if (item.name == SettingScreenItem.About.name) {
                                        uriHandler.openUri("https://github.com/noti-summary/NotiSummary")
                                    } else if (item.name == SettingScreenItem.Privacy.name) {
                                        uriHandler.openUri("https://example.com")
                                    } else if (item.name == SettingScreenItem.Recruitment.name) {
                                        uriHandler.openUri("https://forms.gle/5pY6BBqpsSfZQ2LJA")
                                    } else {
                                        navController.navigate(item.name)
                                    }
                                },
                            shape = RoundedCornerShape(
                                topStart = if (idx  == 0 || idx == 5) 12.dp else 0.dp,
                                topEnd = if (idx  == 0 || idx == 5) 12.dp else 0.dp,
                                bottomStart = if (idx  == 4 || idx == 8) 12.dp else 0.dp,
                                bottomEnd = if (idx  == 4 || idx == 8) 12.dp else 0.dp
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(18.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .size(25.dp),
                                    painter = painterResource(id = item.iconId),
                                    contentDescription = stringResource(item.titleId),
                                )
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth(0.93f),
                                    text = stringResource(item.titleId),
                                )
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowRight,
                                    stringResource(item.titleId)
                                )
                            }
                        }
                }
            }
        }
    }
}